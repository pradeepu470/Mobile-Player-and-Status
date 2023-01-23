package com.pradeep.videoplayercollection;


// common defs for DVB operations
public interface CommonFile
{


    // user database service
    static  final int kUserActionsBase = 4000;
    static final int kActionRegisterUser = kUserActionsBase +1;;
    static final int kActionDeleteUser = kUserActionsBase + 2;
    static final int kActionCheckAdmin = kUserActionsBase + 3;
    static final int kActionSetAdmin = kUserActionsBase + 4;
    static final int kActionVerfyUser = kUserActionsBase + 5;
    static final int kActionChangeSecurity = kUserActionsBase + 6;
    static final int kActionGetSecurity = kUserActionsBase + 7;
    static final int kActionSetFavourite = kUserActionsBase +8;;
    static final int kActionRemoveFavourite = kUserActionsBase + 9;
    static final int kActionCheckFavourite = kUserActionsBase + 10;
    static final int kActionFetchFavourite = kUserActionsBase + 11;
    static final int kActionCheckUserDataVersion = kUserActionsBase + 12;
    static final int kActionSetAdminPin = kUserActionsBase + 13;
    static final int kActionSetReminder = kUserActionsBase + 14;
    static final int kActionDeleteReminder = kUserActionsBase + 15;
    static final int kActionFetchReminder = kUserActionsBase + 16;
    static final int kActionOccurredReminder = kUserActionsBase + 17;
    static final int kActionCheckRecord = kUserActionsBase + 18;
    static final int kActionUpdateRecordView = kUserActionsBase + 19;


    /**Activity notification to user**/
    static final int kPasswordProtected = 1800;
    static final int kUnableToDeleteDifferentUser = 1801;
    static final int kDeletionSuccessful = 1802;
    static final int kDeletionFailed = 1803;
    static final int kChangePauseResume = 1805;
    static final int kRecordedPlayAtPauseResume = 1806;
    static final int kNoMoreRecordingAvailable = 1807;
    static final int kGuidePauseResume = 1808;
    static final int kDeleteRecordingFile = 1809;
    //static final int kPaidChannelPopup = 1810; commented as part of fix by Pradeep. FIx 1185
    static final int kStopRecording = 1810; // Now used by Sonal as fix for issue 1292
    static final int kLogOutPopup = 1811;
    static final int kReminderDeleted = 1812;
    static final int kReminderAdded = 1813;
    static final int kReminderDeletePopup = 1814;
    static final int kReminderDeleteRepeated = 1815;
    static final int kReminderUpdate = 1816;
    static final int kAlreadyAddedReminder = 1817;
    static final int kFailedToFetchData = -1;
    static final int kNoDataFound = 100;

    enum request_parameters {
        WRITE_EVENT_DATA(3001);

        public int val;
        private request_parameters(int value) {
            this.val = value;
        }
    };	 

    enum DB_Status {
        STATUS_OK,
        STATUS_NO_DATA,
        STATUS_ERROR,
    };

}



