package mjm.mjmca.model;

public class GroupModel {

    public String groupId, groupName, groupDescription, groupIcon, groupTimeStamp, groupCreatedBy, groupUN;

    public GroupModel(){

    }

    public GroupModel(String groupId, String groupName, String groupDescription, String groupIcon, String groupTimeStamp, String groupCreatedBy, String groupUN) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.groupIcon = groupIcon;
        this.groupTimeStamp = groupTimeStamp;
        this.groupCreatedBy = groupCreatedBy;
        this.groupUN = groupUN;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getGroupTimeStamp() {
        return groupTimeStamp;
    }

    public void setGroupTimeStamp(String groupTimeStamp) {
        this.groupTimeStamp = groupTimeStamp;
    }

    public String getGroupCreatedBy() {
        return groupCreatedBy;
    }

    public void setGroupCreatedBy(String groupCreatedBy) {
        this.groupCreatedBy = groupCreatedBy;
    }

    public String getGroupUN() {
        return groupUN;
    }

    public void setGroupUN(String groupUN) {
        this.groupUN = groupUN;
    }
}
