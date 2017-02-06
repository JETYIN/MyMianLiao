package com.tjut.mianliao.forum.components;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;

import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.components.PlanetCollegeView.Area;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class PlanetFillTool {

    PlanetCollegeView[] mViews;

    ArrayList<PlanetCollege> mCollegeInfoList;
    Context mContext;

    int mMyAreaId;
    private boolean mIsGetSuc;

    private static final int[] sPlaneName = { R.string.plc_planet1_name, R.string.plc_planet2_name,
            R.string.plc_planet3_name, R.string.plc_planet4_name, R.string.plc_planet5_name };

    public PlanetFillTool(Context context, PlanetCollegeView[] views) {
        mViews = views;
        mCollegeInfoList = new ArrayList<PlanetCollege>();
        mContext = context;
        new ListUnlockAreaTask().executeLong();
    }

    public void fetchData() {
        if (!mIsGetSuc) {
            new ListUnlockAreaTask().executeLong();
        }
    }
    
    private void processPlanetCollegeView() {
        int myAreaPos = -1;

        for (int i = Area.West; i <= Area.Center; i++) {
            if (i == mMyAreaId) {
                myAreaPos = i;
            }
        }

        myAreaPos -= 10;

        PlanetCollege planetCollege = mCollegeInfoList.get(myAreaPos);
        mCollegeInfoList.remove(planetCollege);
        mCollegeInfoList.add(myAreaPos, planetCollege);
//        exchangeData(myAreaPos);
        for (int i = 0; i < mViews.length; i++) {
            setPlanetCollegeView(mViews[i], mCollegeInfoList.get(i));
        }
    }

    private void exchangeData(int pos) {
        PlanetCollege pcMine = mCollegeInfoList.get(pos);
        PlanetCollege pcCenter = mCollegeInfoList.get(2);
        mCollegeInfoList.remove(pos);
        mCollegeInfoList.add(pos, pcCenter);
        mCollegeInfoList.add(2, pcMine);
        int resPos = sPlaneName[pos];
        int resCenter = sPlaneName[2];
        sPlaneName[pos] = resCenter;
        sPlaneName[2] = resPos;
    }

    private void setPlanetCollegeView(PlanetCollegeView view, PlanetCollege planetCollege) {
        view.setUnlocked(planetCollege.isUnlocked);
        switch (planetCollege.planetId) {
            case Area.West: {
                view.setTitle(R.string.plc_planet1_name);
                if (planetCollege.isUnlocked) {
                	view.setTitleColor(0XFF00FFFF);
                	view.setTitleBackground(R.drawable.pic_name_choose);
                } else {
                	view.setTitleColor(0XFF8D8E8E);
                }
//                view.setSpace();

            }
                break;

            case Area.East: {
                view.setTitle(R.string.plc_planet2_name);
                if (planetCollege.isUnlocked) {
                	view.setTitleColor(0XFF00FFFF);
                	view.setPlanImage(R.drawable.pic_star_yellow);
                } else {
                	view.setPlanImage(R.drawable.pic_star_yellow_lock);
                	view.setTitleColor(0XFF8D8E8E);
                }

            }
                break;

            case Area.North: {
                view.setTitle(R.string.plc_planet3_name);
                if (planetCollege.isUnlocked) {
                	view.setTitleColor(0XFF00FFFF);
                	view.setPlanImage(R.drawable.pic_star_purple);
                } else {
                	view.setTitleColor(0XFF8D8E8E);
                	view.setPlanImage(R.drawable.pic_star_purple_lock);
                }

            }
                break;

            case Area.South: {
                view.setTitle(R.string.plc_planet4_name);
                if (planetCollege.isUnlocked) {
                	view.setTitleColor(0XFF00FFFF);
                	view.setPlanImage(R.drawable.pic_star_pink);
                } else {
                	view.setTitleColor(0XFF8D8E8E);
                	view.setPlanImage(R.drawable.pic_star_pink_lock);
                }

            }
                break;

            case Area.Center: {
                view.setTitle(R.string.plc_planet5_name);
                if (planetCollege.isUnlocked) {
                	view.setTitleColor(0XFF00FFFF);
                	view.setPlanImage(R.drawable.pic_star_brown);
                } else {
                	view.setTitleColor(0XFF8D8E8E);
                	view.setPlanImage(R.drawable.pic_star_brown_lock);
                }

            }
                break;
        }
    }

    private class ListUnlockAreaTask extends MsTask {

        public ListUnlockAreaTask() {
            super(mContext, MsRequest.SCHOOL_UNLOCK_LIST);

        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                try {
                    JSONObject jo = new JSONObject(response.response);
                    mMyAreaId = jo.optInt("my");
                    String unlockAreas = jo.optString("unlocked");
                    mCollegeInfoList.clear();
                    for (int i = Area.West; i <= Area.Center; i++) {
                        PlanetCollege planetCollege = new PlanetCollege();
                        planetCollege.setPlanetId(i);
                        planetCollege.setIs_unlocked(unlockAreas.contains(Integer.toString(i)));
                        mCollegeInfoList.add(planetCollege);
                    }
                    processPlanetCollegeView();
                    mIsGetSuc = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    mIsGetSuc = false;
                }
            } else {
                mMyAreaId = 10;
                mCollegeInfoList.clear();
                for (int i = Area.West; i <= Area.Center; i++) {
                    PlanetCollege planetCollege = new PlanetCollege();
                    planetCollege.setPlanetId(i);
                    planetCollege.setIs_unlocked(false);
                    mCollegeInfoList.add(planetCollege);
                }
                processPlanetCollegeView();
                mIsGetSuc = false;
            }
        }
    }

    class PlanetCollege {

        int planetId;
        boolean isUnlocked;

        public int getPlanetId() {
            return planetId;
        }

        public void setPlanetId(int planetId) {
            this.planetId = planetId;
        }

        public boolean isUnlocked() {
            return isUnlocked;
        }

        public void setIs_unlocked(boolean isUnlocked) {
            this.isUnlocked = isUnlocked;
        }

    }

}
