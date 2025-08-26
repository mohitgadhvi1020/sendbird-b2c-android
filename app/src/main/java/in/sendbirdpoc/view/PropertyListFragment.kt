package `in`.sendbirdpoc.view

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.params.GroupChannelCreateParams
import com.sendbird.android.params.GroupChannelListQueryParams
import com.sendbird.android.params.UserMessageCreateParams
import com.sendbird.android.user.User
import com.sendbird.uikit.fragments.ChannelFragment
import com.sendbird.uikit.interfaces.providers.ChannelModuleProvider
import com.sendbird.uikit.modules.ChannelModule
import com.sendbird.uikit.providers.ModuleProviders
import dagger.hilt.android.AndroidEntryPoint
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.adapter.PropertyAdapter
import `in`.sendbirdpoc.adapter.PropertyFilterOptionsAdapter
import `in`.sendbirdpoc.api.utils.State
import `in`.sendbirdpoc.base.BaseFragment
import `in`.sendbirdpoc.databinding.FragmentPropertyListBinding
import `in`.sendbirdpoc.model.PropertyListResponse
import `in`.sendbirdpoc.viewmodels.PropertyListViewModel

@AndroidEntryPoint
class PropertyListFragment :
    BaseFragment<FragmentPropertyListBinding>(R.layout.fragment_property_list) {

    private val viewModel by viewModels<PropertyListViewModel>()
    private lateinit var adapterPropertyList: PropertyAdapter
    private lateinit var adapterPropertyFilterOptionsAdapter: PropertyFilterOptionsAdapter

    override fun setUpViews() {
        adapterPropertyList = PropertyAdapter(
            mutableListOf(),
            object : PropertyAdapter.OnItemClickListener {
                override fun onItemCLick(
                    item: PropertyListResponse.Property, position: Int
                ) {

                }

                override fun onChatCLick(
                    item: PropertyListResponse.Property,
                    position: Int
                ) {
                    if (!pref.isLoggedIn) {
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        requireActivity().finish()
                    } else {
                        createOrOpenChannel(
                            requireContext(),
                            item,
                            item.id,
                            item.title,
                            item.agent_send_bird_id
                        )
                    }
                }
            })

        binding.rvPropertyList.adapter = adapterPropertyList


        adapterPropertyFilterOptionsAdapter = PropertyFilterOptionsAdapter(
            mutableListOf(),
            object : PropertyFilterOptionsAdapter.OnItemClickListener {
                override fun onItemCLick(
                    data: String,
                    position: Int
                ) {

                }
            })

        binding.rvPropertyFilter.adapter = adapterPropertyFilterOptionsAdapter

        adapterPropertyFilterOptionsAdapter.setData(
            listOf(
                "Rent",
                "Property Type",
                "Price",
                "Beds & Baths"
            )
        )
    }

    private fun createOrOpenChannel(
        context: Context,
        propertyDetails: PropertyListResponse.Property,
        propertyId: String,
        channelName: String,
        targetUserId: String
    ) {

        val query = GroupChannel.createMyGroupChannelListQuery(
            GroupChannelListQueryParams().apply {
                customTypesFilter = listOf("property_chat_${propertyId}")
            }
        )

        query.next { channels, e ->
            if (e != null) {
            } else {
                val matchedChannel = channels?.firstOrNull()
                if (matchedChannel == null) {

                    val params = GroupChannelCreateParams().apply {
                        userIds = listOf(targetUserId)
                        name = channelName
                        operatorUserIds = listOf(pref.loginUserSendbirdId ?: "")
                        customType = "property_chat_${propertyId}"
                        data = Gson().toJson(propertyDetails)
                        isDistinct = false
                    }

                    GroupChannel.createChannel(params) { channel, e ->
                        if (e != null || channel == null) {
                            Toast.makeText(
                                context,
                                "Failed to create channel: ${e?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@createChannel
                        }
                        val onlineMemberCount = channel.members.count {
                            it.connectionStatus == User.ConnectionStatus.ONLINE
                        }

                        channel.addOperators(listOf(targetUserId)) { addOperatorError -> }

                        ModuleProviders.channel = ChannelModuleProvider { context, _ ->
                            val module = ChannelModule(context)
                            module.setHeaderComponent(
                                CustomChannelHeaderComponent(
                                    onlineMemberCount = onlineMemberCount,
                                    onAddMemberClick = {
                                        showProgressDialog()
                                        val query = channel.createMutedUserListQuery(
                                            limit = 5
                                        )

                                        if (query.hasNext) {
                                            query.next { users: List<User>?, e: SendbirdException? ->
                                                val isUserMuted =
                                                    users?.any { it.userId == (pref.loginUserSendbirdId) } == true

                                                if (isUserMuted) {
                                                    hideProgressDialog()
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "You are block in this channel",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    hideProgressDialog()
                                                    startActivity(
                                                        Intent(
                                                            requireContext(),
                                                            ManageMemberActivity::class.java
                                                        ).putExtra("channelUrl", channel.url)
                                                    )
                                                }
                                            }
                                        }
                                    }, onBackClick = {
                                        requireActivity()
                                            .supportFragmentManager
                                            .popBackStack()
                                    })
                            )
                            module
                        }

                        val channelData = Gson().fromJson<PropertyListResponse.Property>(
                            channel.data,
                            object : TypeToken<PropertyListResponse.Property>() {}.type
                        )
                        val params = UserMessageCreateParams().apply {
                            message =
                                "${channelData.title}\n ${channelData.price} AED | Bed: ${channelData.features?.bedrooms ?: ""} | Bath: ${channelData.features?.bathrooms} | Area: ${channelData.features?.square_feet} sqft"
                        }

                        channel.sendUserMessage(params) { message, e ->
                            if (e != null) {
                                Log.e("Sendbird", "Error sending default message: ${e.message}")
                            } else {
                                Log.d("Sendbird", "Default message sent: ${message?.message}")
                            }
                        }

                        /*if (channelData.images.isNotEmpty()) {
                            val imageMessageParam = UserMessageCreateParams().apply {
                                message = channelData.images[0]
                            }

                            channel.sendUserMessage(imageMessageParam) { message, e ->
                                if (e != null) {
                                    Log.e("Sendbird", "Error sending default message: ${e.message}")
                                } else {
                                    Log.d("Sendbird", "Default message sent: ${message?.message}")
                                }
                            }
                        }*/

                        val channelFragment = ChannelFragment.Builder(channel.url).build()

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fcvHome, channelFragment)
                            .addToBackStack(channelFragment::class.java.simpleName)
                            .commit()
                    }

                } else {

                    val onlineMemberCount = matchedChannel.members.count {
                        it.connectionStatus == User.ConnectionStatus.ONLINE
                    }

                    ModuleProviders.channel = ChannelModuleProvider { context, _ ->
                        val module = ChannelModule(context)
                        module.setHeaderComponent(
                            CustomChannelHeaderComponent(
                                onlineMemberCount = onlineMemberCount,
                                onAddMemberClick = {

                                    showProgressDialog()
                                    val query = matchedChannel.createMutedUserListQuery(
                                        limit = 5
                                    )

                                    var isUserMuted = false

                                    if (query.hasNext) {
                                        query.next { users: List<User>?, e: SendbirdException? ->
                                            if (e != null) {
                                                Log.e(
                                                    "Sendbird",
                                                    "Failed to fetch muted users: ${e.message}"
                                                )
                                                return@next
                                            }

                                            isUserMuted =
                                                users?.any { it.userId == (pref.loginUserSendbirdId) } == true
                                        }
                                    }

                                    if (isUserMuted) {
                                        hideProgressDialog()
                                        Toast.makeText(
                                            requireContext(),
                                            "You are block in this channel",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        hideProgressDialog()
                                        startActivity(
                                            Intent(
                                                requireContext(),
                                                ManageMemberActivity::class.java
                                            ).putExtra("channelUrl", matchedChannel.url)
                                        )
                                    }
                                }, onBackClick = {
                                    requireActivity()
                                        .supportFragmentManager
                                        .popBackStack()
                                })
                        )
                        module
                    }



                    val channelFragment = ChannelFragment.Builder(matchedChannel.url).build()

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fcvHome, channelFragment)
                        .addToBackStack(channelFragment::class.java.simpleName)
                        .commit()
                }
            }
        }
    }

    override fun setUpObservers() {

        viewModel.propertyListResponse.observe(this) { response ->
            when (response) {
                is State.Loading -> {
                    showProgressDialog()
                }

                is State.Error -> {
                    hideProgressDialog()
                }

                is State.Success -> {
                    hideProgressDialog()
                    val propertyList = response.data.data.properties
                    adapterPropertyList.setData(propertyList)
                    binding.tvNumOfProperties.text = "${propertyList.size} properties"
                }

                else -> {

                }
            }
        }
    }

    override fun performApiCall() {
        viewModel.getPropertyList()
    }
}