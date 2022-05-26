package gr.aueb.simplegram.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import gr.aueb.simplegram.common.values.Message;
import gr.aueb.simplegram.common.values.MultimediaFile;
import gr.aueb.simplegram.common.values.Story;
import gr.aueb.simplegram.common.values.Value;

/**
 * Basic class that contains all necessary information
 * and data structures for terminals in order to communicate
 * with brokers.
 */
public class UserNode {

    Socket cbtSocket = null;
    ObjectInputStream cbtIn = null;
    ObjectOutputStream cbtOut = null;

    private String username;
    private Context context;
    
    // Local data structures
    private HashMap<String, Topic> topics;

    // The list of brokers that the node requires
    // in order to publish and consume data.
    private ArrayList<InetAddress> brokerAddresses;
    private HashMap<InetAddress, BrokerConnection> brokerConnections;


    public UserNode(Context context, String username) {
        this.context = context;
        this.username = username;
        this.topics = new HashMap<String, Topic>();
        this.brokerAddresses = new ArrayList<InetAddress>();
        this.brokerConnections = new HashMap<InetAddress, BrokerConnection>();
    }

    /**
     * This method starts the various daemon threads.
     */
    public void userStart() {
        StoryChecker sc = new StoryChecker(this.topics);
        sc.start();
    }

    /**
     * This method is used to break a multimediafile in chunks.
     * @param path the multimedia file path
     * @return ArrayList of chunk. Each chunk is a byte array.
     */
    public static ArrayList<byte[]> chunkify(String path) {

        ArrayList<byte[]> chunks = new ArrayList<byte[]>();

        try {
            int bytes = 0;
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);

            // break file into chunks
            byte[] buffer = new byte[512 * 1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                chunks.add(buffer.clone());
            }

            fileInputStream.close();

        } catch (Exception e){
            e.printStackTrace();
        }

        return chunks;
    }

    // API methods

    /**
     * This method is used by the terminal in order to subscribe
     * to a topic.
     * @param topicname the name of the topic to subscribe to
     */
    public void sub(String topicname){

        boolean requestComplete = false;
        int brokerID = (int) (Math.random() * (this.brokerConnections.size()));
        InetAddress brokerAddress = null;


        while (!requestComplete){

            synchronized (this.brokerConnections){
                for(BrokerConnection bc : this.brokerConnections.values()){
                    if(bc.getBrokerID() == brokerID)
                        brokerAddress = bc.getBrokerAddress();
                }
            }

            try {
                if(brokerAddress == null){
                    throw new ConnectException();
                }

                String brokerIp = brokerAddress.toString().split("/")[1];
                // TODO: debug
                //System.out.println("Attempting to send to Broker with IP: "+brokerIp);
                cbtSocket = new Socket();
                cbtSocket.connect(new InetSocketAddress(brokerIp, 5001), 3000);
                cbtOut = new ObjectOutputStream(cbtSocket.getOutputStream());
                cbtIn = new ObjectInputStream(cbtSocket.getInputStream());


                cbtOut.writeUTF("SUB");
                cbtOut.flush();

                cbtOut.writeUTF(this.username);
                cbtOut.flush();

                cbtOut.writeUTF(topicname);
                cbtOut.flush();

                String confirmationToProceed = cbtIn.readUTF();
                if (confirmationToProceed.equals("CONFIRM")) {
                    // the broker is the correct...
                    synchronized (this.topics) {
                        this.topics.put(topicname, new Topic(topicname));
                        this.topics.get(topicname).setAssignedBrokerID(brokerID);
                        this.topics.get(topicname).addUser(this.username);
                    }
                    String reply = cbtIn.readUTF();
                    //TODO: debug
                    Log.d("TAGHERE", "SERVER REPLY"+reply);

                    //System.out.println(reply);
                    requestComplete = true;
                } else if (confirmationToProceed.equals("DENY")) {
                    // Get correct broker...
                    int correctBrokerToCommTo = Integer.parseInt(cbtIn.readUTF());
                    brokerID = correctBrokerToCommTo;
                    // TODO: debug
                    //System.out.println("Broker not responsible for topic... communicating with broker #" + correctBrokerToCommTo);
                }


            } catch(SocketTimeoutException ste) {
                Log.d("TAGHERE", ""+ste.getMessage());
                //ste.printStackTrace();
                //System.out.println("TIMEOUT Broker unreachable or dead...");
                synchronized (this.topics) {
                    Topic t = this.topics.get(topicname);
                    //System.out.println("previous BrokerID: "+t.getAssignedBrokerID());
                    t.setAssignedBrokerID((t.getAssignedBrokerID() + 1) % (this.brokerConnections.size()));
                    //System.out.println("next BrokerID: "+t.getAssignedBrokerID());
                }
                //System.out.println("Assigning to next alive broker and trying again...");

            } catch(ConnectException ce){
                Log.d("TAGHERE", ""+ce.getMessage());

                //System.out.println("Broker unreachable or dead...");
                synchronized (this.topics) {
                    Topic t = this.topics.get(topicname);
                    //System.out.println("previous BrokerID: "+t.getAssignedBrokerID());
                    t.setAssignedBrokerID((t.getAssignedBrokerID() + 1) % (this.brokerConnections.size()));
                    //System.out.println("next BrokerID: "+t.getAssignedBrokerID());
                }
                //System.out.println("Assigning to next alive broker and trying again...");

            }catch (Exception e) {
                //e.printStackTrace();
                Log.d("TAGHERE", ""+e.getMessage());

            } finally {
                try {
                    if (cbtIn != null)
                        cbtIn.close();
                    if (cbtOut != null)
                        cbtOut.close();
                    if (cbtSocket != null){
                        if (!cbtSocket.isClosed()) {
                            cbtSocket.close();
                        }
                    }
                }catch (IOException e){
                    //e.printStackTrace();
                }
            }
        }

    }

    /**
     * This method is used by the terminal in order to unsubscribe
     * from a topic. If the user is not subscribed to the topic, the
     * method has no effect.
     * @param topicname the name of the topic to unsubscribe from
     */
    public void unsub(String topicname){
        synchronized (this.topics){
            if(!this.topics.get(topicname).isSubbed(this.username)){
                return;
            }
        }

        boolean requestComplete = false;
        int brokerID;
        InetAddress brokerAddress = null;

        while (!requestComplete) {

            synchronized (this.topics) {
                brokerID = this.topics.get(topicname).getAssignedBrokerID();
            }

            synchronized (this.brokerConnections) {
                for (BrokerConnection bc : this.brokerConnections.values()) {
                    if (bc.getBrokerID() == brokerID)
                        brokerAddress = bc.getBrokerAddress();
                }
            }



            try {
                if(brokerAddress == null){
                    throw new ConnectException();
                }

                String brokerIp = brokerAddress.toString().split("/")[1];
                //System.out.println("Attempting to send to Broker with IP: "+brokerIp);
                this.cbtSocket = new Socket();
                this.cbtSocket.connect(new InetSocketAddress(brokerIp, 5001), 3000);
                this.cbtOut = new ObjectOutputStream(cbtSocket.getOutputStream());
                this.cbtIn = new ObjectInputStream(cbtSocket.getInputStream());

                this.cbtOut.writeUTF("UNSUB");
                this.cbtOut.flush();

                this.cbtOut.writeUTF(this.username);
                this.cbtOut.flush();


                this.cbtOut.writeUTF(topicname);
                this.cbtOut.flush();


                // server reply
                String confirmationToProceed = this.cbtIn.readUTF();
                if(confirmationToProceed.equals("CONFIRM")){
                    synchronized (this.topics){
                        this.topics.remove(topicname);
                    }

                    String reply = this.cbtIn.readUTF();
                    //System.out.println(reply);
                    requestComplete = true;
                } else if (confirmationToProceed.equals("DENY")){
                    int correctBrokerToCommTo = Integer.parseInt(this.cbtIn.readUTF());
                    synchronized (this.topics) {
                        this.topics.get(topicname).setAssignedBrokerID(correctBrokerToCommTo);
                    }
                    //System.out.println("Broker not responsible for topic... communicating with broker #" + correctBrokerToCommTo);
                }


            } catch(SocketTimeoutException ste) {
                ste.printStackTrace();
                //System.out.println("TIMEOUT Broker unreachable or dead...");
                synchronized (this.topics) {
                    Topic t = this.topics.get(topicname);
                    //System.out.println("previous BrokerID: "+t.getAssignedBrokerID());
                    t.setAssignedBrokerID((t.getAssignedBrokerID() + 1) % (this.brokerConnections.size()));
                    //System.out.println("next BrokerID: "+t.getAssignedBrokerID());
                }
                //System.out.println("Assigning to next alive broker and trying again...");

            } catch(ConnectException ce){
                ce.printStackTrace();
                //System.out.println("Broker unreachable or dead...");
                synchronized (this.topics) {
                    Topic t = this.topics.get(topicname);
                    //System.out.println("previous BrokerID: "+t.getAssignedBrokerID());
                    t.setAssignedBrokerID((t.getAssignedBrokerID() + 1) % (this.brokerConnections.size()));
                    //System.out.println("next BrokerID: "+t.getAssignedBrokerID());
                }
                //System.out.println("Assigning to next alive broker and trying again...");

            }catch (Exception e) {
                //e.printStackTrace();
            } finally {
                try {
                    if (this.cbtIn != null)
                        this.cbtIn.close();
                    if (this.cbtOut != null)
                        this.cbtOut.close();
                    if (this.cbtSocket != null){
                        if (!this.cbtSocket.isClosed()) {
                            this.cbtSocket.close();
                        }
                    }
                }catch (IOException e){
                    //e.printStackTrace();
                }
            }

            // END WHILE
        }
    }

    /**
     * This method is a daemon that pulls the latest unread messages
     * from all the brokers in the network.
     */
    public void pull(){
        // TODO: implement
        for(BrokerConnection bc : this.brokerConnections.values()){
            PullHandler pullh = new PullHandler(bc,this.topics, this.username);
            pullh.start();
        }
    }

    /**
     * This method is used to push a value to a topic.
     * @param topicname the name of the topic to push the value to
     * @param val the value [Message/MultimediaFile/Story] to be pushed
     */
    public void pushValue(String topicname, Value val){
        boolean requestComplete = false;
        int brokerID;
        InetAddress brokerAddress = null;


        while (!requestComplete) {

            synchronized (this.topics) {
                brokerID = this.topics.get(topicname).getAssignedBrokerID();
            }

            synchronized (this.brokerConnections) {
                for (BrokerConnection bc : this.brokerConnections.values()) {
                    if (bc.getBrokerID() == brokerID)
                        brokerAddress = bc.getBrokerAddress();
                }
            }

            try {
                if(brokerAddress == null){
                    throw new ConnectException();
                }

                String brokerIp = brokerAddress.toString().split("/")[1];
                //System.out.println("Attempting to send to Broker with IP: "+brokerIp);
                this.cbtSocket = new Socket();
                this.cbtSocket.connect(new InetSocketAddress(brokerIp, 5001), 3000);
                this.cbtOut = new ObjectOutputStream(cbtSocket.getOutputStream());
                this.cbtIn = new ObjectInputStream(cbtSocket.getInputStream());

                // Declare request...
                this.cbtOut.writeUTF("PUSH");
                this.cbtOut.flush();

                // Declare user
                this.cbtOut.writeUTF(this.username);
                this.cbtOut.flush();

                // Declare topic
                this.cbtOut.writeUTF(topicname);
                this.cbtOut.flush();

                String confirmationToProceed = this.cbtIn.readUTF();
                if(confirmationToProceed.equals("CONFIRM")){
                    if(val instanceof Message){
                        this.cbtOut.writeUTF("MSG");
                        this.cbtOut.flush();

                        this.cbtOut.writeObject(val);
                        this.cbtOut.flush();


                    } else if(val instanceof Story) {
                        this.cbtOut.writeUTF("STORY");
                        this.cbtOut.flush();

                        sendFile((Story) val);

                    } else if(val instanceof MultimediaFile) {
                        this.cbtOut.writeUTF("MULTIF");
                        this.cbtOut.flush();

                        sendFile((MultimediaFile) val);

                    }

                    String brokerReply = this.cbtIn.readUTF();
                    //System.out.println(brokerReply);
                    requestComplete = true;

                } else {
                    // Get correct broker...
                    int correctBrokerToCommTo = Integer.parseInt(this.cbtIn.readUTF());
                    synchronized (this.topics) {
                        this.topics.get(topicname).setAssignedBrokerID(correctBrokerToCommTo);
                    }
                    //System.out.println("Broker not responsible for topic... communicating with broker #" + correctBrokerToCommTo);
                }



            } catch(SocketTimeoutException ste) {
                //ste.printStackTrace();
                //System.out.println("TIMEOUT Broker unreachable or dead...");
                synchronized (this.topics) {
                    Topic t = this.topics.get(topicname);
                    //System.out.println("previous BrokerID: "+t.getAssignedBrokerID());
                    t.setAssignedBrokerID((t.getAssignedBrokerID() + 1) % (this.brokerConnections.size()));
                    //System.out.println("next BrokerID: "+t.getAssignedBrokerID());
                }
                //System.out.println("Assigning to next alive broker and trying again...");

            } catch(ConnectException ce){
                ce.printStackTrace();
                //System.out.println("Broker unreachable or dead...");
                synchronized (this.topics) {
                    Topic t = this.topics.get(topicname);
                    //System.out.println("previous BrokerID: "+t.getAssignedBrokerID());
                    t.setAssignedBrokerID((t.getAssignedBrokerID() + 1) % (this.brokerConnections.size()));
                    //System.out.println("next BrokerID: "+t.getAssignedBrokerID());
                    System.out.println(t.getAssignedBrokerID());
                }
                //System.out.println("Assigning to next alive broker and trying again...");

            }catch (Exception e) {
                //e.printStackTrace();
            } finally {
                try {
                    if (this.cbtIn != null)
                        this.cbtIn.close();
                    if (this.cbtOut != null)
                        this.cbtOut.close();
                    if (this.cbtSocket != null){
                        if (!this.cbtSocket.isClosed()) {
                            this.cbtSocket.close();
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * This method is used to push a multimedia file to a topic.
     * It sends the multimedia file's metadata and chunks separately.
     * @param mf2send the multimediafile object
     */
    private void sendFile(MultimediaFile mf2send){
        try {
            int bytes = 0;
            ArrayList<byte[]> chunks = mf2send.getChunks();
            MultimediaFile mf2send_empty;


            if(mf2send instanceof Story){
                mf2send_empty = new Story(
                        mf2send.getDateSent(),
                        mf2send.getSentFrom(),
                        mf2send.getFilename(),
                        chunks.size(),
                        new ArrayList<byte[]>()
                );
            } else {
                mf2send_empty = new MultimediaFile(
                        mf2send.getDateSent(),
                        mf2send.getSentFrom(),
                        mf2send.getFilename(),
                        chunks.size(),
                        new ArrayList<byte[]>()
                );
            }
            System.out.println("new chunks: "+mf2send_empty.getChunks().size());

            this.cbtOut.writeObject(mf2send_empty);
            this.cbtOut.flush();

            for (int i = 0; i < chunks.size(); i++) {
                System.out.println("Sending chunk #" + i);
                this.cbtOut.write(chunks.get(i), 0, 512 * 1024);
                this.cbtOut.flush();

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    // SUBSCRIBER FAMILY THREADS:
    /**
     * This is the parent class for handling Subscriber-oriented actions.
     */
    class SubscriberHandler extends Thread {
        Socket cbtSocket;
        ObjectInputStream cbtIn;
        ObjectOutputStream cbtOut;

        String username;

        public SubscriberHandler(
                String username
        ) {
            this.username = username;
        }

    }

    private abstract class SubscriberTask extends AsyncTask<Void, Void, Void> {
        Socket cbtSocket;
        ObjectInputStream cbtIn;
        ObjectOutputStream cbtOut;

        String username;

        public SubscriberTask(
                String username
        ) {
            this.username = username;
        }
    }


    /**
     * This class handles the terminal's 'PULL' request to a broker.
     */
    class PullHandler extends SubscriberHandler {

        HashMap<String, Topic> topics;
        BrokerConnection brokerConnection;

        public PullHandler(
                BrokerConnection brokerConnection,
                HashMap<String, Topic> topics,
                String username
                ) {
            super(username);
            this.topics = topics;
            this.brokerConnection = brokerConnection;
        }

        /**
         * This method is used in order to receive a multimedia file
         * from a broker.
         * @param val_type the type of multimedia file to receive [MULTIF/STORY]
         * @return MultimediaFile object
         * @throws Exception
         */
        private MultimediaFile receiveFile(String val_type) throws Exception{//data transfer with chunking

            MultimediaFile mf_rcv;
            if(val_type.equals("MULTIF")){
                mf_rcv = (MultimediaFile) this.cbtIn.readObject();
            } else {
                mf_rcv = (Story) this.cbtIn.readObject();
            }

            int size = mf_rcv.getFileSize();// amount of expected chunks
            String filename = mf_rcv.getFilename();// read file name

            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            byte[] buffer = new byte[512*1024]; //512 * 2^10 (512KByte chunk size)

            while (size>0) {
                this.cbtIn.readFully(buffer, 0, 512*1024);
                fileOutputStream.write(buffer,0,512*1024);
                size --;
            }
            fileOutputStream.close();
            return mf_rcv;
        }

        /**
         * This method is a daemon that periodically pulls the latest values
         * from a broker.
         */
        @Override
        public void run() {



            while(true) {
                //if(brokerConnection.isActive()){
                try {
                    // TODO: if timeout then make broker dead. Might not make it in the final version.
                    String brokerIp = this.brokerConnection.getBrokerAddress().toString().split("/")[1];
                    this.cbtSocket = new Socket();
                    this.cbtSocket.connect(new InetSocketAddress(brokerIp, 5001), 3000);
                    this.cbtOut = new ObjectOutputStream(cbtSocket.getOutputStream());
                    this.cbtIn = new ObjectInputStream(cbtSocket.getInputStream());
                    // TODO: debug
                    //System.out.println("Pulling recent values from Broker #" + brokerConnection.getBrokerID() + " (" + brokerIp + ")");
                    HashMap<String, ArrayList<Value>> unreads = new HashMap<String, ArrayList<Value>>();

                    this.cbtOut.writeUTF("PULL");
                    this.cbtOut.flush();

                    this.cbtOut.writeUTF(this.username);
                    this.cbtOut.flush();

                    do {
                        String topic_name = this.cbtIn.readUTF();
                        if (topic_name.equals("---"))
                            break;
                        String val_type = this.cbtIn.readUTF();
                        Value v = null;
                        if (val_type.equals("MSG")) {
                            v = (Message) this.cbtIn.readObject();
                        } else if (val_type.equals("MULTIF") || val_type.equals("STORY")) {
                            v = this.receiveFile(val_type);
                        }

                        // add to unread queue
                        if (unreads.get(topic_name) == null)
                            unreads.put(topic_name, new ArrayList<Value>());
                        unreads.get(topic_name).add(v);


                    } while (!this.cbtIn.readUTF().equals("---"));

                    synchronized (this.topics) {
                        for (String topicName : unreads.keySet()) {
                            Topic localTopic = this.topics.get(topicName);
                            ArrayList<Value> unreadValues = unreads.get(topicName);
                            for (Value val : unreadValues) {
                                if (val instanceof Story) {
                                    localTopic.addStory((Story) val);
                                    //TODO: debug
                                    //System.out.println(TerminalColors.ANSI_GREEN + val.getSentFrom() + "@" + topicName + ": (STORY) " + val + TerminalColors.ANSI_RESET);
                                } else {
                                    localTopic.addMessage(val);
                                    // TODO: debug
                                    //System.out.println(TerminalColors.ANSI_GREEN + val.getSentFrom() + "@" + topicName + ": " + val + TerminalColors.ANSI_RESET);
                                }
                            }
                        }
                    }

                } catch(SocketTimeoutException ste) {
                    // TODO: Fault tolerance? Might not make it in the final version.
                    /*synchronized (brokerConnection){
                        brokerConnection.setDead();
                    }*/
                    System.out.println("PULL FAIL: Broker #" + brokerConnection.getBrokerID()+" (" + brokerConnection.getBrokerAddress().toString()+") might be dead...");
                }catch (Exception e) {
                    //e.printStackTrace();
                } finally {
                    try{
                        if(this.cbtIn!=null)
                            this.cbtIn.close();
                        if(this.cbtOut!=null)
                            this.cbtOut.close();
                        if(this.cbtSocket != null)
                            if (!this.cbtSocket.isClosed()){
                                this.cbtSocket.close();
                            }
                    }catch (IOException e){
                        //e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //}

        }
    }

    private class PullTask extends SubscriberTask {

        HashMap<String, Topic> topics;
        BrokerConnection brokerConnection;

        public PullTask(
                BrokerConnection brokerConnection,
                HashMap<String, Topic> topics,
                String username
        ) {
            super(username);
            this.topics = topics;
            this.brokerConnection = brokerConnection;
        }

        /**
         * This method is used in order to receive a multimedia file
         * from a broker.
         * @param val_type the type of multimedia file to receive [MULTIF/STORY]
         * @return MultimediaFile object
         * @throws Exception
         */
        private MultimediaFile receiveFile(String val_type) throws Exception{//data transfer with chunking

            MultimediaFile mf_rcv;
            if(val_type.equals("MULTIF")){
                mf_rcv = (MultimediaFile) this.cbtIn.readObject();
            } else {
                mf_rcv = (Story) this.cbtIn.readObject();
            }

            int size = mf_rcv.getFileSize();// amount of expected chunks
            String filename = mf_rcv.getFilename();// read file name

            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            byte[] buffer = new byte[512*1024]; //512 * 2^10 (512KByte chunk size)

            while (size>0) {
                this.cbtIn.readFully(buffer, 0, 512*1024);
                fileOutputStream.write(buffer,0,512*1024);
                size --;
            }
            fileOutputStream.close();
            return mf_rcv;
        }

        /**
         * This method is a daemon that periodically pulls the latest values
         * from a broker.
         */

        @Override
        protected Void doInBackground(Void... voids) {
            while(true) {
                //if(brokerConnection.isActive()){
                try {
                    // TODO: if timeout then make broker dead. Might not make it in the final version.
                    String brokerIp = this.brokerConnection.getBrokerAddress().toString().split("/")[1];
                    this.cbtSocket = new Socket();
                    this.cbtSocket.connect(new InetSocketAddress(brokerIp, 5001), 3000);
                    this.cbtOut = new ObjectOutputStream(cbtSocket.getOutputStream());
                    this.cbtIn = new ObjectInputStream(cbtSocket.getInputStream());
                    // TODO: debug
                    //System.out.println("Pulling recent values from Broker #" + brokerConnection.getBrokerID() + " (" + brokerIp + ")");
                    HashMap<String, ArrayList<Value>> unreads = new HashMap<String, ArrayList<Value>>();

                    this.cbtOut.writeUTF("PULL");
                    this.cbtOut.flush();

                    this.cbtOut.writeUTF(this.username);
                    this.cbtOut.flush();

                    do {
                        String topic_name = this.cbtIn.readUTF();
                        if (topic_name.equals("---"))
                            break;
                        String val_type = this.cbtIn.readUTF();
                        Value v = null;
                        if (val_type.equals("MSG")) {
                            v = (Message) this.cbtIn.readObject();
                        } else if (val_type.equals("MULTIF") || val_type.equals("STORY")) {
                            v = this.receiveFile(val_type);
                        }

                        // add to unread queue
                        if (unreads.get(topic_name) == null)
                            unreads.put(topic_name, new ArrayList<Value>());
                        unreads.get(topic_name).add(v);


                    } while (!this.cbtIn.readUTF().equals("---"));

                    synchronized (this.topics) {
                        for (String topicName : unreads.keySet()) {
                            Topic localTopic = this.topics.get(topicName);
                            ArrayList<Value> unreadValues = unreads.get(topicName);
                            for (Value val : unreadValues) {
                                if (val instanceof Story) {
                                    localTopic.addStory((Story) val);
                                    //TODO: debug
                                    //System.out.println(TerminalColors.ANSI_GREEN + val.getSentFrom() + "@" + topicName + ": (STORY) " + val + TerminalColors.ANSI_RESET);
                                } else {
                                    localTopic.addMessage(val);
                                    // TODO: debug
                                    //System.out.println(TerminalColors.ANSI_GREEN + val.getSentFrom() + "@" + topicName + ": " + val + TerminalColors.ANSI_RESET);
                                }
                            }
                        }
                    }

                } catch(SocketTimeoutException ste) {
                    // TODO: Fault tolerance? Might not make it in the final version.
                    /*synchronized (brokerConnection){
                        brokerConnection.setDead();
                    }*/
                    System.out.println("PULL FAIL: Broker #" + brokerConnection.getBrokerID()+" (" + brokerConnection.getBrokerAddress().toString()+") might be dead...");
                }catch (Exception e) {
                    //e.printStackTrace();
                } finally {
                    try{
                        if(this.cbtIn!=null)
                            this.cbtIn.close();
                        if(this.cbtOut!=null)
                            this.cbtOut.close();
                        if(this.cbtSocket != null)
                            if (!this.cbtSocket.isClosed()){
                                this.cbtSocket.close();
                            }
                    }catch (IOException e){
                        //e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //}

        }
    }
}