package com.wooriss.woorifood2.Model;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

//Each custom class must have a public constructor that takes no arguments. In addition, the class must include a public getter for each property.
public class Review {
    private String reviewerUid;
    private double taste;

    private int price;
    private int visit;
    private int luxury;
    private int complex;

    private String comment;

    @ServerTimestamp private Timestamp timestamp; // server timestamp

    public Review() {}

    public Review(String reviewerUid, double taste, int price, int visit, int luxury, int complex, String comment) {
        this.reviewerUid = reviewerUid;
        this.taste = taste;
        this.price = price;
        this.visit = visit;
        this.luxury = luxury;
        this.complex = complex;
        this.comment = comment;
    }

    public double getTaste() {
        return taste;
    }

    public String getReviewerUid() {
        return reviewerUid;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getComplex() {
        return complex;
    }

    public int getPrice() {
        return price;
    }

    public int getVisit() {
        return visit;
    }

    public int getLuxury() {
        return luxury;
    }

    public String getComment() {
        return comment;
    }

}
