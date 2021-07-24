package ge.lbukhnikashvili.androidfinalproject.MVP

import com.google.firebase.database.DataSnapshot

interface IMainPresenter {
    abstract fun addUser(uid:String,nickname:String,profession:String,icon:String)
    abstract fun requestUsersBriefInfo()
    abstract fun retrievedUsersBriefInfo(data:DataSnapshot?)
}