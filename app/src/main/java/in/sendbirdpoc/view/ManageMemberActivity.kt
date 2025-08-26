package `in`.sendbirdpoc.view

import android.app.Dialog
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.ApplicationUserListQueryParams
import com.sendbird.android.params.MutedUserListQueryParams
import com.sendbird.android.user.Member
import com.sendbird.android.user.User
import dagger.hilt.android.AndroidEntryPoint
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.adapter.AllMemberAdapter
import `in`.sendbirdpoc.adapter.ExistingMemberAdapter
import `in`.sendbirdpoc.base.BaseActivity
import `in`.sendbirdpoc.databinding.ActivityManageMemberBinding
import `in`.sendbirdpoc.databinding.DialogMemberLimitReachedBinding
import `in`.sendbirdpoc.databinding.DialogRemoveMemberConfirmationBinding
import `in`.sendbirdpoc.databinding.DialogRemoveMemberSucccesFailBinding
import `in`.sendbirdpoc.util.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class ManageMemberActivity :
    BaseActivity<ActivityManageMemberBinding>(R.layout.activity_manage_member) {

    private lateinit var adapterExistingMemberAdapter: ExistingMemberAdapter
    private lateinit var adapterAllMemberAdapter: AllMemberAdapter
    private var channelUrl = ""
    private var channelExistingMember = mutableListOf<String>()
    private var blockedUserIds = listOf<String>()
    private var isRedirectToMain = false

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isRedirectToMain) {
                    startActivity(
                        Intent(
                            this@ManageMemberActivity,
                            MainActivity::class.java
                        ).putExtra("isFromMemberList", true)
                    )
                    finish()
                } else {
                    finish()
                }
            }
        }

    override fun setUpViews() {
        channelUrl = intent.getStringExtra("channelUrl") ?: ""
        adapterExistingMemberAdapter = ExistingMemberAdapter(
            mutableListOf(),
            loggedInUserId = pref.loginUserSendbirdId,
            object : ExistingMemberAdapter.OnItemClickListener {
                override fun onMemberRemove(
                    data: Member,
                    position: Int
                ) {
                    /*if (data.userId == pref.loginUserSendbirdId) {
                        showMemberRemoveSuccessFailDialog(false)
                    } else*/ if (data.metaData["role"] == "agent" && adapterExistingMemberAdapter.getList()
                            .count { it.metaData["role"] == "agent" } == 1
                    ) {
                        showMemberRemoveSuccessFailDialog(false)
                    } else if (data.metaData["role"] == "user" && adapterExistingMemberAdapter.getList()
                            .count { it.metaData["role"] == "user" } == 1
                    ) {
                        showMemberRemoveSuccessFailDialog(false)
                    } else {

                        showProgressDialog()
                        var isUserBlocked: Boolean

                        val query = SendbirdChat.createMutedUserListQuery(
                            MutedUserListQueryParams(
                                limit = 10,
                                channelUrl = channelUrl,
                                channelType = ChannelType.GROUP
                            )
                        )

                        if (query.hasNext) {
                            query.next { users, e2 ->
                                if (e2 != null) {
                                    hideProgressDialog()
                                    return@next
                                }
                                isUserBlocked = users?.any { it.userId == data.userId } == true
                                hideProgressDialog()
                                showRemoveMemberConfirmationDialog(data.userId, isUserBlocked)
                            }
                        }
                    }
                }
            })

        binding.rvMember.adapter = adapterExistingMemberAdapter

        adapterAllMemberAdapter = AllMemberAdapter(
            mutableListOf(),
            channelExistingMember,
            object : AllMemberAdapter.OnItemClickListener {
                override fun isShowLimitReachedDialog(
                    data: User,
                    position: Int
                ) {
                    showMemberLimitReachedDialog()
                }
            })

        binding.rvAddMemberList.adapter = adapterAllMemberAdapter

        val query = SendbirdChat.createMutedUserListQuery(
            MutedUserListQueryParams(
                limit = 10,
                channelUrl = channelUrl,
                channelType = ChannelType.GROUP
            )
        )

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        query.next { users, e ->
            if (e != null) {
                setMemberData()
                return@next
            }

            if (users != null) {
                blockedUserIds = users.map { it.userId }
                setMemberData()
            }
        }
    }

    private fun setMemberData() {

        GroupChannel.getChannel(channelUrl) { channel, e ->
            if (e != null) {
                return@getChannel
            }
            val memberList = channel?.members ?: emptyList()
            adapterExistingMemberAdapter.setData(memberList, blockedUserIds)
            channelExistingMember = memberList.map { it.userId }.toMutableList()

            val query = SendbirdChat.createApplicationUserListQuery(
                ApplicationUserListQueryParams().apply {}
            )

            if (query.hasNext) {
                query.next { users, e2 ->
                    if (e2 != null) return@next
                    adapterAllMemberAdapter.setData(users ?: emptyList(), channelExistingMember)
                }
            }
        }
    }

    private fun removeMemberUsingApi(channelUrl: String, userId: String) {

        RetrofitClient.apiService.removeUser(
            "c5431a514a98ef393b4a6e86adc68c5b3e59f617",
            channelUrl, mutableMapOf(
                "user_ids" to listOf(userId),
                "reason" to "admin_removed"
            )
        ).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {

                    if (pref.loginUserSendbirdId == userId) {
                        isRedirectToMain = true
                    }

                    showMemberRemoveSuccessFailDialog(true)
                    setMemberData()
                } else {
                    showMemberRemoveSuccessFailDialog(false)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e("PUT", "Error: ${t.message}")
            }
        })
    }

    private fun blockUnblockUser(userId: String, isBlock: Boolean) {

        if (isBlock) {
            RetrofitClient.apiService.blockUser(
                "c5431a514a98ef393b4a6e86adc68c5b3e59f617",
                pref.loginUserSendbirdId ?: "", mutableMapOf(
                    "target_id" to userId
                )
            ).enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ManageMemberActivity,
                            "User block successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        blockedUserIds.plus(userId).let {
                            blockedUserIds = it
                        }
                        setMemberData()
                    } else {
                        Toast.makeText(
                            this@ManageMemberActivity,
                            "Failed to block user",
                            Toast.LENGTH_SHORT
                        ).show()
                        setMemberData()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("PUT", "Error: ${t.message}")
                }
            })
        } else {
            RetrofitClient.apiService.unBlockUser(
                "c5431a514a98ef393b4a6e86adc68c5b3e59f617",
                pref.loginUserSendbirdId ?: "", userId
            ).enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ManageMemberActivity,
                            "User unblock successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        blockedUserIds.minus(userId).let {
                            blockedUserIds = it
                        }
                        setMemberData()
                    } else {
                        Toast.makeText(
                            this@ManageMemberActivity,
                            "Failed to unblock user",
                            Toast.LENGTH_SHORT
                        ).show()
                        setMemberData()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("PUT", "Error: ${t.message}")
                }
            })
        }
    }

    private fun muteUnmuteUser(userId: String, isMute: Boolean) {

        if (isMute) {
            RetrofitClient.apiService.muteUser(
                "c5431a514a98ef393b4a6e86adc68c5b3e59f617",
                channelUrl,
                mutableMapOf(
                    "user_id" to userId,
                    "seconds" to Int.MAX_VALUE
                )
            ).enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ManageMemberActivity,
                            "User block successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        blockedUserIds.plus(userId).let {
                            blockedUserIds = it
                        }
                        setMemberData()
                    } else {
                        Toast.makeText(
                            this@ManageMemberActivity,
                            "Failed to block user",
                            Toast.LENGTH_SHORT
                        ).show()
                        setMemberData()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("PUT", "Error: ${t.message}")
                }
            })
        } else {
            RetrofitClient.apiService.unmuteUSer(
                "c5431a514a98ef393b4a6e86adc68c5b3e59f617",
                channelUrl,
                userId
            ).enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ManageMemberActivity,
                            "User unblock successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        blockedUserIds.minus(userId).let {
                            blockedUserIds = it
                        }
                        setMemberData()
                    } else {
                        Toast.makeText(
                            this@ManageMemberActivity,
                            "Failed to unblock user",
                            Toast.LENGTH_SHORT
                        ).show()
                        setMemberData()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("PUT", "Error: ${t.message}")
                }
            })
        }
    }

    private fun showMemberLimitReachedDialog() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val binding: DialogMemberLimitReachedBinding =
            DialogMemberLimitReachedBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(binding.root)

        binding.apply {
            ivClose.setOnClickListener {
                dialog.dismiss()
            }
            tvManageMember.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()

        val window: Window = dialog.window!!
        window.setGravity(Gravity.BOTTOM)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun showMemberRemoveSuccessFailDialog(isSuccess: Boolean) {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val binding: DialogRemoveMemberSucccesFailBinding =
            DialogRemoveMemberSucccesFailBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(binding.root)

        binding.apply {

            val drawable = if (isSuccess) {
                AppCompatResources.getDrawable(
                    this@ManageMemberActivity,
                    R.drawable.ic_check
                )
            } else {
                AppCompatResources.getDrawable(
                    this@ManageMemberActivity,
                    R.drawable.ic_cross
                )
            }

            val title = if (isSuccess) {
                "Member removed successfully"
            } else {
                "Unable to Remove Member"
            }

            val description = if (isSuccess) {
                "They no longer have access to the group or its shared resources."
            } else {
                "This action cannot be completed at this time."
            }

            ivInfo.setImageDrawable(drawable)
            tvTitle.text = title
            tvDescription.text = description

            ivClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

            val window: Window = dialog.window!!
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    private fun showRemoveMemberConfirmationDialog(userId: String, isUserBlocked: Boolean) {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val binding = DialogRemoveMemberConfirmationBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(binding.root)

        binding.apply {

            if (isUserBlocked) {
                tvBlock.text = "Unblock"
            } else {
                tvBlock.text = "Block"
            }

            tvRemoveMember.setOnClickListener {
                dialog.dismiss()

                removeMemberUsingApi(channelUrl, userId)

                /*GroupChannel.getChannel(channelUrl) { channel, e ->
                    if (e != null) {
                        return@getChannel
                    }

                    channel?.banUser(userId, "Removed by admin", 0) { banError ->
                        if (banError != null) {
                            banError.printStackTrace()
                            Toast.makeText(
                                this@ManageMemberActivity,
                                banError.message ?: "",
                                Toast.LENGTH_SHORT
                            ).show()
                            showMemberRemoveSuccessFailDialog(false)
                        } else {
                            showMemberRemoveSuccessFailDialog(true)
                            setMemberData()
                        }
                    }
                }*/
            }

            tvBlock.setOnClickListener {
                dialog.dismiss()

                if (tvBlock.text == "Unblock") {





                    muteUnmuteUser(userId, false)
                } else {



                    muteUnmuteUser(userId, true)
                }

                ivClose.setOnClickListener {
                    dialog.dismiss()
                }
            }

            ivClose.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()

        val window: Window = dialog.window!!
        window.setGravity(Gravity.BOTTOM)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun setUpListeners() {
        binding.tvAddMember.setOnClickListener {
            GroupChannel.getChannel(channelUrl) { channel, e ->
                if (e != null) {
                    return@getChannel
                }

                val newlySelectedMember = adapterAllMemberAdapter.getSelectedUserIds()
                    .filter {
                        !adapterExistingMemberAdapter.getList().map { list -> list.userId }
                            .contains(it)
                    }

                channel?.invite(newlySelectedMember) { error ->
                    if (error != null) {
                        error.printStackTrace()
                    } else {
                        channel.addOperators(newlySelectedMember) { addOperatorError ->
                            if (addOperatorError == null) {
                                Toast.makeText(
                                    this@ManageMemberActivity,
                                    "User added successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

}