package gr.aueb.simplegram.common;

import java.util.ArrayList;
import java.util.HashMap;

import com.simplegram.src.Story;
import com.simplegram.src.Value;

public class Topic {
    // Topic data structures
    private String name;
    private ArrayList<String> subscribers;
    private HashMap<String, Integer> indexInTopic; // usernode index in topic messages
    private HashMap<String, Integer> indexInStories; // usernode index in stories
    private ArrayList<Value> messageQueue;
    private ArrayList<Story> storyQueue;
    private int assignedBrokerID;

    public Topic(String name) {
        this.name = name;
        this.subscribers = new ArrayList<String>();
        this.messageQueue = new ArrayList<Value>();
        this.storyQueue = new ArrayList<Story>();
        this.indexInTopic = new HashMap<String, Integer>();
        this.indexInStories = new HashMap<String, Integer>();
        this.assignedBrokerID = -1;
    }

    /**
     * This method returns the assigned broker's id.
     * @return the assigned broker's id.
     */
    public int getAssignedBrokerID() {
        return this.assignedBrokerID;
    }

    /**
     * This method is used to set the responsible broker for this topic.
     * @param newAssignedBrokerID the ID of the responsible broker.
     */
    public void setAssignedBrokerID(int newAssignedBrokerID) {
        this.assignedBrokerID = newAssignedBrokerID;
    }

    /**
     * This method adds new subscribers to the topic.
     * @param user username
     */
    public void addUser(String user) {
        this.subscribers.add(user);
        this.indexInTopic.put(user, 0);
        this.indexInStories.put(user, 0);
    }

    /**
     * This method removes subscribers from the topic.
     * @param user username
     */
    public void removeUser(String user) {
        this.subscribers.remove(user);
        this.indexInTopic.remove(user);
        this.indexInStories.remove(user);
    }

    /**
     * This method is used to push a new text message to
     * the topic.
     * @param message the message object
     */
    public void addMessage(Value message) {
        this.messageQueue.add(message);
    }

    /**
     * This method is used to push a new story to
     * the topic.
     * @param story
     */
    public void addStory(Story story){
        this.storyQueue.add(story);
    }

    public ArrayList<Value> getMessageQueue() {
        return this.messageQueue;
    }

    public ArrayList<Story> getStoryQueue() {
        return this.storyQueue;
    }

    public ArrayList<String> getSubscribers() {
        return this.subscribers;
    }

    /**
     * This method is used to check if a user is subbed to a topic.
     * @param user_name
     * @return user subscription status
     */
    public boolean isSubbed(String user_name) {
        return this.subscribers.contains(user_name);
    }

    public int getIndexOfUser(String user){
        return this.indexInTopic.get(user);
    }

    /**
     * This method is used to retrieve all the unread values & stories
     * for a user in the topic.
     * @param user the username
     * @return arraylist of unread values & stories
     */
    public ArrayList<Value> getLatestFor(String user){
        ArrayList<Value> msgs = new ArrayList<Value>();
        int currIndexInTopic = this.indexInTopic.get(user);
        int currIndexInStories = this.indexInStories.get(user);
        for(int i = currIndexInTopic; i<this.messageQueue.size(); i++){
            msgs.add(this.messageQueue.get(i));
        }
        for(int j = currIndexInStories; j<this.storyQueue.size(); j++){
            if(!this.storyQueue.get(j).hasExpired())
                msgs.add(this.storyQueue.get(j));
        }
        this.indexInTopic.put(user, this.messageQueue.size());
        this.indexInStories.put(user, this.storyQueue.size());
        return msgs;
    }

    /**
     * This method returns the latest not viewed story for a user.
     * CLIENT ONLY METHOD.
     * @return
     */
    public Story getLatestStoryFor(){
        Story storyToReturn;
        synchronized (this.storyQueue){
            storyToReturn = this.storyQueue.remove(0);
        }
        return storyToReturn;
    }

    public String getName() {
        return this.name;
    }

    /**
     * This method is used to remove expired stories from the topic.
     */
    public void cleanStories(){
        if(this.storyQueue.size()!=0 && this.storyQueue.get(0).hasExpired()) {
            this.storyQueue.remove(0);
            System.out.println("1 story removed!");
        }
    }
}
