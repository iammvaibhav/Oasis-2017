package v2015.oasis.pilani.bits.com.home.events

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.transition.Fade
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lapism.searchview.SearchAdapter
import com.ramotion.garlandview.TailLayoutManager
import com.ramotion.garlandview.TailSnapHelper
import com.ramotion.garlandview.header.HeaderTransformer
import com.squareup.picasso.Picasso
import v2015.oasis.pilani.bits.com.home.GlobalData
import v2015.oasis.pilani.bits.com.home.GlobalData.categoryFilter
import v2015.oasis.pilani.bits.com.home.GlobalData.categoryWiseData
import v2015.oasis.pilani.bits.com.home.GlobalData.dateWiseData
import v2015.oasis.pilani.bits.com.home.GlobalData.dateWiseOngoingEventsList
import v2015.oasis.pilani.bits.com.home.GlobalData.getInnerDataOfName
import v2015.oasis.pilani.bits.com.home.GlobalData.getSortedAndFilteredList
import v2015.oasis.pilani.bits.com.home.GlobalData.getSuggestionsList
import v2015.oasis.pilani.bits.com.home.GlobalData.tinyDb
import v2015.oasis.pilani.bits.com.home.GlobalData.venueFilter
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.R
import v2015.oasis.pilani.bits.com.home.databinding.FragmentEventsBinding
import v2015.oasis.pilani.bits.com.home.events.inner.InnerData
import v2015.oasis.pilani.bits.com.home.events.outer.OuterAdapter

interface onItemClickListener{
    fun onItemClick(innerData: InnerData)
}

interface IfOpenThenCloseListener{
    fun ifOpenThenClose(): Boolean
}

class EventFragment : Fragment(){

    lateinit var bottomSheetBehaviour: BottomSheetBehavior<CardView>
    lateinit var descriptionBottomSheetBehaviour: BottomSheetBehavior<CardView>
    lateinit var searchBottomSheetBehaviour: BottomSheetBehavior<CardView>
    val selectedColor = Color.parseColor("#B50CFC")
    val deselectedColor = Color.parseColor("#95989A")
    val transitionIn = Fade(Fade.IN)
    val transitionOut = Fade(Fade.OUT)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentEventsBinding.inflate(inflater, container, false)

        val mainActivity = activity as MainActivity
        mainActivity.headerText.text = " EVENTS "
        mainActivity.filter.visibility = View.VISIBLE
        binding.boldTypeface = mainActivity.typefaceBold

        // getting bottom sheet behaviour
        bottomSheetBehaviour = BottomSheetBehavior.from(binding.bottomSheetParent)
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

        descriptionBottomSheetBehaviour = BottomSheetBehavior.from(binding.detailsBottomSheetParent)
        descriptionBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

        searchBottomSheetBehaviour = BottomSheetBehavior.from(binding.searchBottomSheetParent)
        searchBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

        (activity as MainActivity).closeListener = object : IfOpenThenCloseListener {
            override fun ifOpenThenClose(): Boolean {
                if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
                    return true
                }

                if (descriptionBottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
                    descriptionBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
                    return true
                }

                if (searchBottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
                    searchBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
                    return true
                }
                return false
            }
        }

        Picasso.with(activity).load(R.drawable.nav_drawer_background).fit().centerCrop().into(binding.bottomSheetBackground)
        Picasso.with(activity).load(R.drawable.nav_drawer_background).fit().centerCrop().into(binding.descriptionBottomSheetBackground)
        Picasso.with(activity).load(R.drawable.nav_drawer_background).fit().centerCrop().into(binding.searchBottomSheetBackground)


        // GarlandView
        val outerListType = tinyDb.getString("showBy") //categoryWise or dateWise

        binding.tailRecyclerView.layoutManager = TailLayoutManager(context)
        (binding.tailRecyclerView.layoutManager as TailLayoutManager).setPageTransformer(HeaderTransformer())

        val itemClickListener = object : onItemClickListener{
            override fun onItemClick(innerData: InnerData) {
                binding.innerData = innerData
                descriptionBottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }


        val adapter = OuterAdapter(GlobalData.dateWiseData, 1, itemClickListener)
        binding.tailRecyclerView.adapter = adapter

        if (outerListType == "categoryWise") {
            binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.categoryWiseData, 2, itemClickListener)
            binding.showByCategory.setTextColor(selectedColor)
            binding.showByDate.setTextColor(deselectedColor)
        }

        if (arguments != null){
            if (arguments.containsKey("innerData")){
                val innerData = arguments.getSerializable("innerData") as InnerData
                binding.innerData = innerData
                descriptionBottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }else{
                mainActivity.headerText.text = " ONGOING "
                binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.dateWiseOngoingEventsList, 1, itemClickListener)
                mainActivity.filter.visibility = View.INVISIBLE
            }
        }

        TailSnapHelper().attachToRecyclerView(binding.tailRecyclerView)


        // Bottom Sheet
        mainActivity.filter.setOnClickListener {
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.close.setOnClickListener { bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN }
        binding.done.setOnClickListener {
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
            searchBottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            binding.searchView.textOnly = ""
            binding.searchView.open(true)

        }

        binding.showByDate.setOnClickListener {
            binding.showByDate.setTextColor(selectedColor)
            binding.showByCategory.setTextColor(deselectedColor)
            binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.getSortedAndFilteredList("Time", GlobalData.dateWiseData), 1, itemClickListener)
            tinyDb.putString("showBy", "dateWise")
        }

        binding.showByCategory.setOnClickListener {
            binding.showByDate.setTextColor(deselectedColor)
            binding.showByCategory.setTextColor(selectedColor)
            binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.getSortedAndFilteredList("Time", GlobalData.categoryWiseData), 2, itemClickListener)
            tinyDb.putString("showBy", "categoryWise")
        }

        binding.filterByOngoing.setOnClickListener {
            if (binding.showByDate.currentTextColor == selectedColor)
                binding.tailRecyclerView.adapter = OuterAdapter(dateWiseOngoingEventsList, 1, itemClickListener)
            else
                binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.categoryWiseOngoingEventsList, 2, itemClickListener)

            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.filterByFavourites.setOnClickListener {
            if (binding.showByDate.currentTextColor == selectedColor)
                binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.dateWiseFavouriteList, 1, itemClickListener)
            else
            binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.categoryWiseFavouriteList, 2, itemClickListener)

            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.clearAllFilters.setOnClickListener {
            if (binding.showByDate.currentTextColor == selectedColor)
                binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.dateWiseData, 1, itemClickListener)
            else
            binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.categoryWiseData, 2, itemClickListener)

            for ((i, _) in categoryFilter)
                categoryFilter[i] = true

            for ((i, _) in venueFilter)
                venueFilter[i] = true

            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        }



        val callbackDate = { binding.tailRecyclerView.adapter = OuterAdapter(getSortedAndFilteredList("Time", dateWiseData), 1, itemClickListener) }
        val callbackCategory = { binding.tailRecyclerView.adapter = OuterAdapter(getSortedAndFilteredList("Time", categoryWiseData), 2, itemClickListener) }

        binding.bsRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.filterByCategory.setOnClickListener {
            /*if (GlobalData.categoryFilter.isNotEmpty()){
                binding.selectAll.text = "DESELECT ALL"
            }else{
                binding.selectAll.text = "SELECT ALL"
            }
            binding.selectAll.setOnClickListener { v ->
                if (binding.selectAll.text == "DESELECT ALL"){
                    for (i in GlobalData.categoryFilter.keys){
                        GlobalData.categoryFilter[i] = false
                    }
                    binding.selectAll.text = "SELECT ALL"
                }else{
                    for (i in GlobalData.categoryFilter.keys){
                        GlobalData.categoryFilter[i] = true
                    }
                    binding.selectAll.text = "DESELECT ALL"
                }
                if (binding.showByDate.currentTextColor == selectedColor)
                    (binding.bsRecyclerView.adapter as bsAdapter).changeDatasetTo(GlobalData.categoryFilter, callbackDate)
                else
                    (binding.bsRecyclerView.adapter as bsAdapter).changeDatasetTo(GlobalData.categoryFilter, callbackCategory)
            }*/

            binding.page1.visibility = View.GONE

            if (binding.showByDate.currentTextColor == selectedColor)
                binding.bsRecyclerView.adapter = bsAdapter(GlobalData.categoryFilter, callbackDate)
            else
                binding.bsRecyclerView.adapter = bsAdapter(GlobalData.categoryFilter, callbackCategory)

            binding.page2.visibility = View.VISIBLE

        }

        binding.filterByVenue.setOnClickListener {

            /*if (GlobalData.venueFilter.isNotEmpty()){
                binding.selectAll.text = "DESELECT ALL"
            }else{
                binding.selectAll.text = "SELECT ALL"
            }
            binding.selectAll.setOnClickListener { v ->
                if (binding.selectAll.text == "DESELECT ALL"){
                    for (i in GlobalData.venueFilter.keys){
                        GlobalData.venueFilter[i] = false
                    }
                    binding.selectAll.text = "SELECT ALL"
                }else{
                    for (i in GlobalData.venueFilter.keys){
                        GlobalData.venueFilter[i] = true
                    }
                    binding.selectAll.text = "DESELECT ALL"
                }
                if (binding.showByDate.currentTextColor == selectedColor)
                    (binding.bsRecyclerView.adapter as bsAdapter).changeDatasetTo(GlobalData.categoryFilter, callbackDate)
                else
                    (binding.bsRecyclerView.adapter as bsAdapter).changeDatasetTo(GlobalData.categoryFilter, callbackCategory)
            }*/

            binding.page1.visibility = View.GONE

            if (binding.showByDate.currentTextColor == selectedColor)
                binding.bsRecyclerView.adapter = bsAdapter(GlobalData.venueFilter, callbackDate)
            else
                binding.bsRecyclerView.adapter = bsAdapter(GlobalData.venueFilter, callbackCategory)

            binding.page2.visibility = View.VISIBLE
        }

        binding.back.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.bottomSheetParent, transitionOut)
            binding.page2.visibility = View.GONE

            TransitionManager.beginDelayedTransition(binding.bottomSheetParent, transitionIn)
            binding.page1.visibility = View.VISIBLE
        }

        binding.done2.setOnClickListener {
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
            binding.page2.visibility = View.GONE
            binding.page1.visibility = View.VISIBLE
        }

        binding.close2.setOnClickListener{
            descriptionBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.searchView.setOnQueryTextListener(object : com.lapism.searchview.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                if (binding.showByDate.currentTextColor == selectedColor)
                    binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.getSearchResults(query, dateWiseData), 1, itemClickListener)
                else
                    binding.tailRecyclerView.adapter = OuterAdapter(GlobalData.getSearchResults(query, categoryWiseData), 2, itemClickListener)


                bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
                return true
            }

            override fun onQueryTextChange(newText: String) = false
        })

        binding.searchView.setOnNavigationIconClickListener {
            searchBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        }

        val searchAdapter = SearchAdapter(activity, getSuggestionsList())
        searchAdapter.setOnSearchItemClickListener { view, position, text ->
            //searchBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
            binding.innerData = getInnerDataOfName(text)
            binding.searchView.close(true)
            binding.searchView.textOnly = ""
            descriptionBottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            view.postDelayed({
                searchBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
            }, 100)
        }



        binding.searchView.adapter = searchAdapter

        return binding.root
    }
}