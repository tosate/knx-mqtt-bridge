package de.devtom.app.knxmqttbridge.knx;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.device.KnxDeviceServiceLogic;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.exception.KNXException;

public class KnxSwitchingService extends KnxDeviceServiceLogic {
	private static final Logger LOGGER = LoggerFactory.getLogger(KnxSwitchingService.class);
	
	private List<CommandDP> commandDatpoints;
	private List<StateDP> stateDatapoints;
	private KnxDeviceManager knxDeviceManager;
	
	private boolean switchedOn;
	
	public KnxSwitchingService(List<GroupAddress> switchingGaList, List<GroupAddress> listeningGaList, KnxDeviceManager knxManager) {
		this.commandDatpoints = new ArrayList<CommandDP>();
		this.stateDatapoints = new ArrayList<StateDP>();
		this.knxDeviceManager = knxManager;
		
		for(GroupAddress ga : switchingGaList) {
			CommandDP cmdDp = new CommandDP(ga, "Switching");
			cmdDp.setDPT(0, DPTXlatorBoolean.DPT_SWITCH.getID());
			this.addCommandDp(cmdDp);
		}
		
		for(GroupAddress ga : listeningGaList) {
			StateDP stateDp = new StateDP(ga, "State");
			stateDp.setDPT(0, DPTXlatorBoolean.DPT_STATE.getID());
			this.addStateDp(stateDp);
		}
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

	@Override
	public void updateDatapointValue(Datapoint ofDp, DPTXlator update) {
		if(ofDp instanceof StateDP) {
			switchedOn = ((DPTXlatorBoolean) update).getValueBoolean();
		} else if(ofDp instanceof CommandDP) {
			boolean switchCommand = ((DPTXlatorBoolean) update).getValueBoolean();
			knxDeviceManager.switchService(ofDp.getMainAddress().toString(), switchCommand);
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("{} datapoint value got updated to \"{}\"", ofDp.getName(), update.getValue());
		}
	}

	@Override
	public DPTXlator requestDatapointValue(Datapoint ofDp) throws KNXException {
		final DPTXlatorBoolean t = new DPTXlatorBoolean(ofDp.getDPT());
		t.setValue(switchedOn);
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Respond with \"{}\" to read request for {}", t.getValue(), ofDp.getName());
		}
		return t;
	}

}