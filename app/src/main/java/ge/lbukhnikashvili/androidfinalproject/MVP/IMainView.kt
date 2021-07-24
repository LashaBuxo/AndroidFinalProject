package ge.lbukhnikashvili.androidfinalproject.MVP
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo

interface IMainView {
    fun usersBriefInfoReceived(data: MutableList<UserInfo>)
}