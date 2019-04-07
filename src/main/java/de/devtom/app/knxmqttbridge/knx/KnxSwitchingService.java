package de.devtom.app.knxmqttbridge.knx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.device.KnxDeviceServiceLogic;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.dptxlator.DPTXlatorSceneNumber;
import tuwien.auto.calimero.exception.KNXException;

public class KnxSwitchingService extends KnxDeviceServiceLogic implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(KnxSwitchingService.class);
	private static final String SWITCHING_DP = "Switching DP";
	private static final String STATE_DP = "State DP";
	private static final String SCENES_DP = "Scenes DP";
	
	private IndividualAddress individualAdress;
	private List<CommandDP> commandDatpoints;
	private List<StateDP> stateDatapoints;
	private KnxDeviceManager knxDeviceManager;
	private Map<Double, Boolean> scenes;
	
	private boolean switchedOn;
	
	public KnxSwitchingService(IndividualAddress individualAdress, List<GroupAddress> switchingGaList, List<GroupAddress> listeningGaList, KnxDeviceManager knxManager) {
		this(individualAdress, switchingGaList, listeningGaList, null, knxManager);
	}
	
	public KnxSwitchingService(IndividualAddress individualAdress, List<GroupAddress> switchingGaList, List<GroupAddress> listeningGaList, GroupAddress sceneGa, KnxDeviceManager knxManager) {
		this.individualAdress = individualAdress;
		this.commandDatpoints = new ArrayList<CommandDP>();
		this.stateDatapoints = new ArrayList<StateDP>();
		this.knxDeviceManager = knxManager;
		
		for(GroupAddress ga : switchingGaList) {
			CommandDP cmdDp = new CommandDP(ga, SWITCHING_DP);
			cmdDp.setDPT(0, DPTXlatorBoolean.DPT_SWITCH.getID());
			this.addCommandDp(cmdDp);
		}
		
		for(GroupAddress ga : listeningGaList) {
			StateDP stateDp = new StateDP(ga, STATE_DP);
			stateDp.setDPT(0, DPTXlatorBoolean.DPT_STATE.getID());
			this.addStateDp(stateDp);
		}
		
		if(sceneGa != null) {
			CommandDP cmdDp = new CommandDP(sceneGa, SCENES_DP);
			cmdDp.setDPT(0, DPTXlatorSceneNumber.DPT_SCENE_NUMBER.getID());
			this.addCommandDp(cmdDp);
		}
		this.scenes = new HashMap<>();
	}
	
	public void addStateDp(StateDP stateDp) {
		if(this.stateDatapoints.contains(stateDp)) {
			return;
		}

		this.stateDatapoints.add(stateDp);
		this.getDatapointModel().add(stateDp);
	}
	
	public void addCommandDp(CommandDP cmdDp) {
		if(this.commandDatpoints.contains(cmdDp)) {
			return;
		}
		
		this.commandDatpoints.add(cmdDp);
		this.getDatapointModel().add(cmdDp);
	}

	public boolean isSwitchedOn() {
		return switchedOn;
	}

	public void setSwitchedOn(boolean switchedOn) {
		this.switchedOn = switchedOn;
	}
	
	public void addScene(int sceneNumber, boolean switchedOn) {
		// scene 1 has value 0.0
		Integer isn = new Integer(sceneNumber - 1);
		Double dsn = isn.doubleValue();
		
		if(this.scenes.containsKey(dsn)) {
			LOGGER.error("Cannot two scenes with scene number: " + sceneNumber);
		} else {
			this.scenes.put(dsn, new  Boolean(switchedOn));
		}
	}
	
	public List<GroupAddress> getListeningGroupAddresses() {
		List<GroupAddress> result = new ArrayList<>();
		
		for(StateDP stateDp : this.stateDatapoints) {
			result.add(stateDp.getMainAddress());
		}
		
		return result;
	}

	@Override
	public void updateDatapointValue(Datapoint ofDp, DPTXlator update) {
		if(ofDp instanceof StateDP) {
			switchedOn = ((DPTXlatorBoolean) update).getValueBoolean();
		} else if(ofDp instanceof CommandDP) {
			if(update instanceof DPTXlatorBoolean) {
				DPTXlatorBoolean booleanUpdate = (DPTXlatorBoolean)update; 
				boolean switchCommand = booleanUpdate.getValueBoolean();
				knxDeviceManager.switchService(individualAdress.toString(), switchCommand);
				
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("{} datapoint value got updated to \"{}\"", ofDp.getName(), update.getValue());
				}
			} else if(update instanceof DPTXlatorSceneNumber) {
				DPTXlatorSceneNumber sceneNumberUpdate = (DPTXlatorSceneNumber)update;
				Double sceneNumber = Double.valueOf(sceneNumberUpdate.getNumericValue());
				
				if(this.scenes.containsKey(sceneNumber)) {
					knxDeviceManager.switchService(individualAdress.toString(), this.scenes.get(sceneNumber).booleanValue());
				}
				
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("{} datapoint value got updated to \"{}\"", ofDp.getName(), update.getValue());
				}
			}
		}
	}

	@Override
	public DPTXlator requestDatapointValue(Datapoint ofDp) throws KNXException {
		DPTXlator result = null;
		
		switch(ofDp.getName()) {
		case SWITCHING_DP:
			final DPTXlatorBoolean b = new DPTXlatorBoolean(ofDp.getDPT());
			b.setValue(switchedOn);
			result = b;
			break;
		case SCENES_DP:
			final DPTXlatorSceneNumber s = new DPTXlatorSceneNumber(ofDp.getDPT());
			s.setValue(1);
			result = s;
			break;
		default:
			throw new KNXException("Request for unknown datapoint: " + ofDp.getName());
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Respond with \"{}\" to read request for {}", result.getValue(), ofDp.getName());
		}
		return result;
	}

	@Override
	public void run() {
		// wait forever, until interrupted
		try {
			synchronized (this) {
				while(true) {
					wait();
				}
			}
		} catch(final InterruptedException e) {
			
		}
	}

}
