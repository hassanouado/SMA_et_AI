package org.example.DF_Service;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class SellerAgent extends Agent {
    private String price;
    @Override
    protected void setup(){
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                price = (String) getArguments()[0];
                DFAgentDescription dfAgentDescription = new DFAgentDescription();
                ServiceDescription firstService = new ServiceDescription();
                firstService.setType("pc");
                firstService.setName("hp");
                dfAgentDescription.addServices(firstService);
                try {
                    DFService.register(this.getAgent(), dfAgentDescription);
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
            ACLMessage receivedMsg = receive();
            if (receivedMsg != null){
                switch (receivedMsg.getPerformative()){
                    case ACLMessage.CFP:
                        ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPOSE);
                        aclMessage.setContent(price);
                        aclMessage.addReceiver(receivedMsg.getSender());
                        send(aclMessage);
                        break;
                    case ACLMessage.ACCEPT_PROPOSAL:
                        ACLMessage firstAclMessage = new ACLMessage(ACLMessage.AGREE);
                        firstAclMessage.setContent("I will sell you this pc");
                        firstAclMessage.addReceiver(receivedMsg.getSender());
                        send(firstAclMessage);
                        break;
                    case ACLMessage.REQUEST:
                        ACLMessage secondAclMessage = new ACLMessage(ACLMessage.CONFIRM);
                        secondAclMessage.setContent("I confirm");
                        secondAclMessage.addReceiver(receivedMsg.getSender());
                        send(secondAclMessage);
                        break;
                }
            }else{
                block();
            }

            }
        });

    }
    @Override
    protected void takeDown()
    {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }
}
