package gr.aueb.simplegram.common.values;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * A simple 'Story' class that contains the required information
 * for a story in a topic.
 */
public class Story extends MultimediaFile implements Serializable {


    public Story(String sentFrom, String filename, int fileSize, ArrayList<byte[]> chunks) {
        super(sentFrom, filename, fileSize, chunks);
    }


    public Story(LocalDateTime dateSent, String sentFrom, String filename, int fileSize, ArrayList<byte[]> chunks) {
        super(dateSent, sentFrom, filename, fileSize, chunks);
    }

    /**
     * This method returns the status of a story. It checks if 1 day has
     * passed since the initial post of the story and if so it declares it
     * expired so that UserNodes can no longer pull it.
     *
     * @return the status of the story [active-true/expired-false]
     */
    public boolean hasExpired(){

        /**
         * If one day has passed since the creation
         * of the story then the story expires and
         * can no longer be pulled by others.
         *
         * In pseudocode:
         *
         *  IF dateNow > dateCreated + 1day THEN:
         *      DELETE;
         *  ELSE:
         *      KEEP;
         */

        return (this.getDateSent().plusDays(1)).compareTo(LocalDateTime.now()) < 0;
    }

    @Override
    public String toString() {
        return super.toString() + " | hasExpired: "+hasExpired();
    }
}
