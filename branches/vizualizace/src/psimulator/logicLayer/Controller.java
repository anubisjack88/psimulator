package psimulator.logicLayer;

import psimulator.dataLayer.DataLayerFacade;
import psimulator.logicLayer.Simulator.SimulatorClientEventRecieverThread;
import psimulator.logicLayer.Simulator.SimulatorPlayerThread;
import psimulator.userInterface.UserInterfaceOuterFacade;

/**
 *
 * @author Martin
 */
public class Controller implements ControllerFacade{

    private DataLayerFacade model;
    private UserInterfaceOuterFacade view;
    
   
    public Controller(DataLayerFacade model, UserInterfaceOuterFacade view) {
        this.model = model;
        this.view = view;

        view.initView((ControllerFacade)this);
        
        SimulatorClientEventRecieverThread eventReciever = new SimulatorClientEventRecieverThread(model, view);
        new Thread(eventReciever).start();
        
        SimulatorPlayerThread simulatorPlayer = new SimulatorPlayerThread(model, view);
        //new Thread(simulatorPlayer).start();
        simulatorPlayer.startThread(new Thread(simulatorPlayer));
        
        model.addSimulatorObserver(eventReciever);
        model.addSimulatorObserver(simulatorPlayer);
    }

    
}
