package com.toyprojects.daychecker

object AppLockState {
    const val varName = "appLockState"
    val ENABLE_PWD = 1001
    val REMOVE_PWD = 1002
    val CHANGE_PWD = 1003
    val START_APP = 1004
}

object EditorState {
    const val varName = "editorState"
    val NEW_RECORD = 2001
    val EDIT_RECORD = 2002
}

object DataBackupState {
    const val varName = "dataBackupState"
    val DATA_EXPORT = 3001
    val DATA_IMPORT = 3002
    val DATA_RESET = 3003
}