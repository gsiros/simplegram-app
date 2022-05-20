package gr.aueb.simplegram.common;

import java.util.HashMap;

public class StoryChecker extends Thread {

    private HashMap<String, Topic> topics;

    public StoryChecker(HashMap<String, Topic> topics){
        this.topics = topics;
    }

    /**
     * This is method is a daemon that periodically checks if a story has expired
     * using the 'cleanStories' method of the Topic class.
     */
    @Override
    public void run() {

        try{

            while(true){
                // TODO: debug
                //System.out.println(TerminalColors.ANSI_CYAN+"Cleaning expired stories..."+TerminalColors.ANSI_RESET);
                synchronized (this.topics) {
                    for(Topic topic : this.topics.values()){
                        topic.cleanStories();
                    }
                }
                Thread.sleep(1000);
            }

        } catch(Exception e) {

        }
    }
}