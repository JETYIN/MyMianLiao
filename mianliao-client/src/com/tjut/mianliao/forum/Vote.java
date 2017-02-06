package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Parcel;

import com.tjut.mianliao.util.JsonUtil;

public class Vote {
    public boolean anony;
    public long endTime;
    public boolean enabled;
    public int[] myVote;
    public long myVoteTime;
    public ArrayList<String> options;
    public int[] result;
    public float[] progress;
    
    public Vote() {}

    public boolean showVoters() {
        return !anony && !enabled;
    }

    /**
     * Make sure the result has same size of options.
     */
    public void verifyResult() {
        if (options != null && options.size() > 0 &&
                (result == null || result.length < options.size())) {
            result = new int[options.size()];
        }
    }

    static Vote fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Vote vote = new Vote();
        vote.anony = json.optInt("anony") == 1;
        vote.endTime = json.optLong("endtime");
        vote.enabled = json.optBoolean("enable");
        vote.myVote = JsonUtil.getIntArray(json.optJSONArray("my_vote"));
        vote.options = JsonUtil.getStringArray(json.optJSONArray("options"));
        vote.result = JsonUtil.getIntArray(json.optJSONArray("result"));
        vote.verifyResult();
        vote.myVoteTime = json.optLong("my_vote_time");
        return vote;
    }

    Vote(Parcel in) {
        anony = in.readInt() == 1;
        endTime = in.readLong();
        enabled = in.readInt() == 1;
        int voteSize = in.readInt();
        if (voteSize > 0) {
            myVote = new int[voteSize];
            in.readIntArray(myVote);
        }
        myVoteTime = in.readLong();
        int optSize = in.readInt();
        if (optSize > 0) {
            options = new ArrayList<String>(optSize);
            in.readStringList(options);
            result = new int[optSize];
            in.readIntArray(result);
        }
    }

    void writeToParcel(Parcel dest) {
        dest.writeInt(anony ? 1 : 0);
        dest.writeLong(endTime);
        dest.writeInt(enabled ? 1 : 0);
        int voteSize = myVote == null ? 0 : myVote.length;
        dest.writeInt(voteSize);
        if (voteSize > 0) {
            dest.writeIntArray(myVote);
        }
        dest.writeLong(myVoteTime);
        int optSize = options == null ? 0 : options.size();
        dest.writeInt(optSize);
        if (optSize > 0) {
            dest.writeStringList(options);
            dest.writeIntArray(result);
        }
    }
}
