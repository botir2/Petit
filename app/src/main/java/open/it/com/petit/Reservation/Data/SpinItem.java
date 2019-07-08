package open.it.com.petit.Reservation.Data;

/**
 * Created by user on 2017-07-10.
 */

public class SpinItem {
    private String text;
    private Integer imageId;

    public SpinItem(String text, Integer imageId) {
        this.text = text;
        this.imageId = imageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }
}
