package jug.domain;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = {
        "likes",
        "unlikes",
        "sum"
})
public class Result {
    private int likes;
    private int unlikes;
    private int sum;

    public Result() {
        // no-op
    }

    public Result(int likes, int unlikes) {
        this.likes = likes;
        this.unlikes = -Math.abs(unlikes);
        sum = likes + unlikes;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getUnlikes() {
        return unlikes;
    }

    public void setUnlikes(int unlikes) {
        this.unlikes = unlikes;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }
}
