package com.sendbird.uikit.internal.testmodel

import androidx.lifecycle.MutableLiveData
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.query.GroupChannelListQuery
import com.sendbird.android.handler.GroupChannelCollectionHandler
import com.sendbird.uikit.internal.contracts.GroupChannelCollectionContract
import com.sendbird.uikit.internal.contracts.SendbirdUIKitContract
import com.sendbird.uikit.internal.contracts.TaskQueueContract

internal interface ViewModelDataContract

internal interface ChannelListViewModelDataContract : ViewModelDataContract {
    val sendbirdUIKit: SendbirdUIKitContract
    var collection: GroupChannelCollectionContract?
    val query: GroupChannelListQuery
    val channelList: MutableLiveData<List<GroupChannel>>
    val collectionHandler: GroupChannelCollectionHandler
    val taskQueue: TaskQueueContract
}
