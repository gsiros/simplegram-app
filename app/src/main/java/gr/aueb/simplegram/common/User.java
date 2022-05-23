package gr.aueb.simplegram.common;

import android.app.Application;

public class User extends Application {
    private UserNode userNode;

    public UserNode getUserNode(){
        return this.userNode;
    }

    public void setUserName(String userName){
        if(userNode == null){
            this.userNode = new UserNode(this, userName);
            this.userNode.init("addr.txt");
        }
    }

}
