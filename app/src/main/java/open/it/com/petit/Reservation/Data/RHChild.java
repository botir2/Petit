package open.it.com.petit.Reservation.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 2017-07-21.
 */

// Expandable child data
public class RHChild implements Parcelable{
    private String content;

    public RHChild(String content) {
        this.content = content;
    }

    protected RHChild(Parcel in) {
        content = in.readString();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RHChild)) return false;

        RHChild artist = (RHChild) o;

        return getContent() != null ? getContent().equals(artist.getContent()) : artist.getContent() == null;
    }

    @Override
    public int hashCode() {
        int result = getContent() != null ? getContent().hashCode() : 0;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
    }

    public static final Creator<RHChild> CREATOR = new Creator<RHChild>() {
        @Override
        public RHChild createFromParcel(Parcel in) {
            return new RHChild(in);
        }

        @Override
        public RHChild[] newArray(int size) {
            return new RHChild[size];
        }
    };
}
