package org.example.DF_Service;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class BuyerAgent extends Agent {
    DFAgentDescription[] dfAgentDescriptions;
    private AID bestSeller;
    private double bestPrice = Double.MAX_VALUE;
    private int count = 0;
    @Override
    protected void setup(){
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription dfAgentDescription = new DFAgentDescription();
                ServiceDescription serviceDescription = new ServiceDescription();
                serviceDescription.setType("pc");
                serviceDescription.setName("hp");
                dfAgentDescription.addServices(serviceDescription);
                try {
                    dfAgentDescriptions = DFService.search(this.getAgent(), dfAgentDescription);
                    for(DFAgentDescription firstdfAgentDescription :dfAgentDescriptions){
                        AID sellerAID = firstdfAgentDescription.getName();
                        ACLMessage aclMessage = new ACLMessage(ACLMessage.CFP);
                        aclMessage.setContent("pc");
                        aclMessage.addReceiver(sellerAID);
                        send(aclMessage);
                    }

                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage receivedMessage = receive();
                if(receivedMessage != null){
                    switch (receivedMessage.getPerformative()){
                        case ACLMessage.PROPOSE:
                            count ++;
                            double price = Double.parseDouble(receivedMessage.getContent());
                            if(price < bestPrice){
                                bestPrice = price;
                                bestSeller = receivedMessage.getSender();
                            }
                            if(dfAgentDescriptions.length == count){
                                ACLMessage aclMessage = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                                aclMessage.addReceiver(bestSeller);
                                aclMessage.setContent(" I accept your proposed price!");
                                send(aclMessage);
                            };
                            break;
                        case ACLMessage.AGREE:
                            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
                            aclMessage.addReceiver(receivedMessage.getSender());
                            aclMessage.setContent("I want to buy this product");
                            send(aclMessage);
                            ;break;
                        case ACLMessage.CONFIRM:
                            System.out.println(receivedMessage.getSender().getName() + " " + receivedMessage.getContent());
                            ; break;



                    }

                }
                else block();
            }
        });

    }
}
