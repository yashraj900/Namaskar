package mjm.mjmca.model;

public class userstate {

    private String date, state, time;

    public userstate(){

    }

    public userstate(String date, String state, String time) {
        this.date = date;
        this.state = state;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
