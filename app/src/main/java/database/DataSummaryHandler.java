package database;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import domain.ExScore;
import domain.ExScoreInfo;
import domain.ExScoreSummary;

public class DataSummaryHandler {
    private static ArrayList<ExScoreSummary> Cards = null;
    public static void loadScoreCardSummary(Context context) {
        if(DataSummaryHandler.Cards == null) {
            DataSummaryHandler.Cards = new ArrayList<>();
        }
        DataSummaryHandler.Cards.clear();
        List<ExScore> scorecards = DBInfo.createInstance(context).readScoreCards();
        if(scorecards != null && scorecards.size() > 0) {
           for(ExScore scorecard : scorecards) {
               ExScoreSummary card = new ExScoreSummary();
               card.UserID = scorecard.UserID;
               card.AppID = scorecard.AppID;
               card.AppNo = scorecard.AppNo;
               card.ScoreCardID = scorecard.ScoreCardID;
               card.GroupDetailID = scorecard.GroupDetailID;
               card.Updated = scorecard.Updated;
               DataSummaryHandler.Cards.add(card);
           }
        }
    }
    public static ArrayList<ExScoreSummary> readScoreCardSummary() {
        if(DataSummaryHandler.Cards == null) { return new ArrayList<ExScoreSummary>(); }
        else { return DataSummaryHandler.Cards; }
    }
    public static void updateScoreCardSummary(ExScore score) {
        if(DataSummaryHandler.Cards != null && score != null) {
            boolean exist = false;
            for(ExScoreSummary card : DataSummaryHandler.Cards) {
                if(card.UserID == score.UserID &&
                    card.AppID == score.AppID &&
                    card.AppNo == score.AppNo &&
                    card.ScoreCardID == score.ScoreCardID &&
                    card.GroupDetailID == score.GroupDetailID) {
                    exist = true;
                    card.Updated = score.Updated;
                }
            }
            if(exist == false) {
                ExScoreSummary card = new ExScoreSummary();
                card.UserID = score.UserID;
                card.AppID = score.AppID;
                card.AppNo = score.AppNo;
                card.ScoreCardID = score.ScoreCardID;
                card.GroupDetailID = score.GroupDetailID;
                card.Updated = score.Updated;
                DataSummaryHandler.Cards.add(card);
            }
        }
    }
}
