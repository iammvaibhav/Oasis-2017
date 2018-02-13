package v2015.oasis.pilani.bits.com.home

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.reflect.TypeToken
import com.lapism.searchview.SearchItem
import v2015.oasis.pilani.bits.com.home.events.inner.InnerData
import v2015.oasis.pilani.bits.com.home.events.outer.OuterData
import v2015.oasis.pilani.bits.com.home.navbar_items.N2OData
import v2015.oasis.pilani.bits.com.home.navbar_items.NotificationData
import v2015.oasis.pilani.bits.com.home.navbar_items.SponsorsData
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object GlobalData {
    var mainActivity: MainActivity? = null

    val TAG = "v2015.oasis|OasisApp"
    private val ONGOING_EVENT_TIME = 3
    var notficationDelayTime = 30
    val eventDataClassType: Type = object : TypeToken<ArrayList<Pair<OuterData, ArrayList<InnerData>>>>(){}.type
    val favouriteClassType: Type = object : TypeToken<HashMap<String, Int>>() {}.type
    val categoryColorsClassType: Type = object : TypeToken<HashMap<String, Int>>(){}.type

    val sponsorsDataType: Type = object : TypeToken<ArrayList<SponsorsData>>(){}.type
    var sponsorsData = ArrayList<SponsorsData>()

    val notificationsDataType: Type = object : TypeToken<ArrayList<NotificationData>>(){}.type
    var notificationsData = ArrayList<NotificationData>()

    val N2OVotingDataType: Type = object : TypeToken<ArrayList<N2OData>>(){}.type
    var N2OVotingData = ArrayList<N2OData>()

    var dateWiseData = ArrayList<Pair<OuterData, ArrayList<InnerData>>>()
    var categoryWiseData = ArrayList<Pair<OuterData, ArrayList<InnerData>>>()

    // favourite data
    var dateWiseFavouriteList = ArrayList<Pair<OuterData, ArrayList<InnerData>>>()
    var categoryWiseFavouriteList = ArrayList<Pair<OuterData, ArrayList<InnerData>>>()

    // ongoing data
    var dateWiseOngoingEventsList = ArrayList<Pair<OuterData, ArrayList<InnerData>>>()
    var categoryWiseOngoingEventsList = ArrayList<Pair<OuterData, ArrayList<InnerData>>>()

    // filtered list based on category and venue filter
    var filteredList = ArrayList<Pair<OuterData, ArrayList<InnerData>>>()

    var categoryFilter = HashMap<String, Boolean>()
    var venueFilter = HashMap<String, Boolean>()

    var favourites = HashMap<String, Int>()
    var categoryColors = HashMap<String, Int>()
    val defaultColor = Color.MAGENTA
    lateinit var tinyDb: TinyDB

    val outerDateComparator = kotlin.Comparator { o1:Pair<OuterData, ArrayList<InnerData>>, o2: Pair<OuterData, ArrayList<InnerData>> ->
        val dateFormat = SimpleDateFormat("MMMM dd")
        try {
            val firstDate = dateFormat.parse(o1.first.heading)
            val secondDate = dateFormat.parse(o2.first.heading)
            return@Comparator firstDate.compareTo(secondDate)
        }catch (e: ParseException){
            e.printStackTrace()
        }
        return@Comparator 1
    }

    val outerCategoryComparator = kotlin.Comparator { o1:Pair<OuterData, ArrayList<InnerData>>, o2:Pair<OuterData, ArrayList<InnerData>> ->
        o1.first.heading.compareTo(o2.first.heading)
    }

    val innerAZComparator = kotlin.Comparator { o1: InnerData, o2: InnerData -> o1.name.compareTo(o2.name) }

    val innerTimeComparator = Comparator { o1: InnerData, o2: InnerData ->
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        try {
            val firstDate = dateFormat.parse("${o1.date} ${o1.time}")
            val secondDate = dateFormat.parse("${o2.date} ${o2.time}")
            return@Comparator firstDate.compareTo(secondDate)
        }catch (e: ParseException){
            e.printStackTrace()
        }
        return@Comparator 1
    }

    fun syncDataAndSaveAndDo(context: Context, afterWork: () -> Unit){

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //Log.e(TAG, "Error")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                GlobalData.dateWiseData.clear()
                //fetching and filling dateWiseData and Meta Data
                for (child in snapshot.child("Events").children) {
                    if (child.childrenCount != 0L) {
                        var date = child.key
                        var color = defaultColor
                        try {
                            color = Color.parseColor((snapshot.child("EventsMetaData").child(date).child("color").value ?: "") as String)
                        } catch (e: IllegalArgumentException) {
                            e.printStackTrace()
                        } catch (e: StringIndexOutOfBoundsException) {
                            e.printStackTrace()
                        }

                        val outFormat = SimpleDateFormat("MMMM dd")
                        val inFormat = SimpleDateFormat("dd-MM-yyyy")
                        try {
                            date = outFormat.format(inFormat.parse(child.key))
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }

                        val outerData = OuterData(date, color)
                        val innerData = ArrayList<InnerData>()

                        for (event in child.children) {
                            try {
                                val name = (event.child("name").value ?: "") as String
                                val category = (event.child("category").value ?: "") as String
                                val desc = (event.child("desc").value ?: "") as String
                                val rules = (event.child("rules").value ?: "") as String
                                val time = (event.child("time").value ?: "") as String
                                val date = (event.child("date").value ?: "") as String
                                val venue = (event.child("venue").value ?: "") as String

                                val key = "$name|$date|$venue"
                                val favouriteState = GlobalData.favourites.containsKey(key)

                                val innerDataItem = InnerData(name, category, getIconOfCategory(category), desc, rules, time, date, venue, favouriteState)
                                innerData.add(innerDataItem)
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                        }
                        dateWiseData.add(Pair(outerData, innerData))
                    }
                }

                for (child in snapshot.child("EventsMetaData").child("Categories").children){
                    var category = child.key
                    var color = defaultColor
                    try {
                        color = Color.parseColor((child.value ?: "") as String)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    } catch (e: StringIndexOutOfBoundsException) {
                        e.printStackTrace()
                    }

                    categoryColors.put(category, color)
                }

                N2OVotingData.clear()

                for (child in snapshot.child("N2O").children){
                    N2OVotingData.add(N2OData((child.child("name").value ?: "") as String, (child.child("image").value ?: "") as String, (child.child("description").value ?: "") as String, (child.child("votes").value ?: 0L) as Long, child.key))
                }

                tinyDb.putBoolean("isN2OAvailable", snapshot.child("N2OStatus").child("available").value as Boolean)
                tinyDb.putString("N2OStatus", (snapshot.child("N2OStatus").child("status").value ?: "") as String)

                tinyDb.putString("aboutSelfieContest", (snapshot.child("SelfieContest").child("Metadata").child("about").value ?: "") as String)


                sponsorsData.clear()
                for (child in snapshot.child("Sponsors").children){
                    val imageURL = (child.child("image").value ?: "") as String
                    val type = (child.child("type").value ?: "") as String
                    val name = (child.child("name").value ?: "") as String
                    val priority = child.key

                    sponsorsData.add(SponsorsData(imageURL, type, name, priority))
                }

                fillCategoryWiseData(dateWiseData, categoryWiseData)
                init()

                val tinyDb = TinyDB(context.applicationContext)

                tinyDb.putBoolean("isCacheDataAvailable", true)
                tinyDb.putObject("dateWiseData", dateWiseData)
                tinyDb.putObject("categoryWiseData", categoryWiseData)
                tinyDb.putObject("categoryColors", categoryColors)
                tinyDb.putObject("sponsorsData", sponsorsData)
                tinyDb.putObject("n2oData", N2OVotingData)

                afterWork()
            }
        }

        FirebaseDatabase.getInstance().reference.addListenerForSingleValueEvent(valueEventListener)
    }

    fun fillCategoryWiseData(dateWiseData: ArrayList<Pair<OuterData, ArrayList<InnerData>>>, categoryWiseData: ArrayList<Pair<OuterData, ArrayList<InnerData>>>){
        categoryWiseData.clear()
        for (x in dateWiseData)
            for (event in x.second){
                val outerData = OuterData(event.category, categoryColors[event.category] ?: defaultColor)
                val index = containsKey(categoryWiseData, outerData)
                if (index != -1)
                    categoryWiseData[index].second.add(event)
                else {
                    val innerItems = ArrayList<InnerData>()
                    innerItems.add(event)
                    categoryWiseData.add(Pair(outerData, innerItems))
                }
            }
    }

    private fun containsKey(data: ArrayList<Pair<OuterData, ArrayList<InnerData>>>, key: OuterData): Int{
        for (i in 0..(data.size - 1))
            if (data[i].first == key)
                return i
        return -1
    }

    fun fillCategoryAndVenueFilters(){
        categoryWiseData.forEach { i -> categoryFilter.put(i.first.heading, true) }
        dateWiseData.forEach { i -> i.second.forEach { j -> if (j.venue !in venueFilter.keys) venueFilter.put(j.venue, true) } }
    }

    fun getSortedAndFilteredList(innerSortType: String, list: ArrayList<Pair<OuterData, ArrayList<InnerData>>>): ArrayList<Pair<OuterData, ArrayList<InnerData>>>{
        filteredList.clear()
        list.forEach { i ->
            val x = ArrayList<InnerData>()
            i.second.forEach { j -> x.add(j) }
            filteredList.add(Pair(i.first, x))
        }

        val iterator = filteredList.iterator()
        while (iterator.hasNext()){
            val pair = iterator.next()
            val innerIterator = pair.second.iterator()
            while (innerIterator.hasNext()) {
                val innerData = innerIterator.next()
                if (categoryFilter[innerData.category] == false || venueFilter[innerData.venue] == false)
                    innerIterator.remove()
            }

            if (pair.second.isEmpty())
                iterator.remove()
        }

        innerSort(innerSortType, list)
        return filteredList
    }

    fun getSearchResults(searchString: String, list: ArrayList<Pair<OuterData, ArrayList<InnerData>>>): ArrayList<Pair<OuterData, ArrayList<InnerData>>>{
        filteredList.clear()
        list.forEach { i ->
            val x = ArrayList<InnerData>()
            i.second.forEach { j -> x.add(j) }
            filteredList.add(Pair(i.first, x))
        }

        val iterator = filteredList.iterator()
        while (iterator.hasNext()){
            val pair = iterator.next()
            val innerIterator = pair.second.iterator()
            while (innerIterator.hasNext()) {
                val innerData = innerIterator.next()
                if (!innerData.name.contains(searchString, false))
                    innerIterator.remove()
            }

            if (pair.second.isEmpty())
                iterator.remove()
        }
        innerSort("Time", list)
        return filteredList
    }

    fun getSuggestionsList(): ArrayList<SearchItem>{
        val suggestionsList = ArrayList<SearchItem>()
        dateWiseData.forEach { i -> i.second.forEach { j -> suggestionsList.add(SearchItem(j.name)) } }
        return suggestionsList
    }

    fun getInnerDataOfName(name: String): InnerData{
        dateWiseData.forEach { i -> i.second.forEach { j -> if (j.name.compareTo(name, true) == 0) return j } }
        return dateWiseData[0].second[0]
    }

    fun innerSort(innerSortType: String, list: ArrayList<Pair<OuterData, ArrayList<InnerData>>>){
        if (innerSortType == "A-Z"){
            for (i in list){
                Collections.sort(i.second, GlobalData.innerAZComparator)
            }
        }else{ //Time or first time.
            for (i in list){
                Collections.sort(i.second, GlobalData.innerTimeComparator)
            }
        }
    }

    fun fillFavouriteList(){
        dateWiseFavouriteList.clear()
        dateWiseData.forEach { i ->
            val x = ArrayList<InnerData>()
            i.second.forEach { j -> x.add(j) }
            dateWiseFavouriteList.add(Pair(i.first, x))
        }

        val iterator = dateWiseFavouriteList.iterator()
        while (iterator.hasNext()){
            val pair = iterator.next()
            val innerIterator = pair.second.iterator()
            while (innerIterator.hasNext()) {
                val innerData = innerIterator.next()
                val key = "${innerData.name}|${innerData.date}|${innerData.venue}"
                if (key !in favourites.keys)
                    innerIterator.remove()
            }

            if (pair.second.isEmpty())
                iterator.remove()
        }

        fillCategoryWiseData(dateWiseFavouriteList, categoryWiseFavouriteList)
        Collections.sort(categoryWiseFavouriteList, outerCategoryComparator)
        innerSort("Time", categoryWiseFavouriteList)
    }

    fun fillOngoingEventList(){
        dateWiseOngoingEventsList.clear()
        dateWiseData.forEach { i ->
            val x = ArrayList<InnerData>()
            i.second.forEach { j -> x.add(j) }
            dateWiseOngoingEventsList.add(Pair(i.first, x))
        }

        val iterator = dateWiseOngoingEventsList.iterator()
        while (iterator.hasNext()){
            val pair = iterator.next()
            val innerIterator = pair.second.iterator()
            while (innerIterator.hasNext()) {
                val innerData = innerIterator.next()
                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm")
                var date = Date()
                val currTime = Date()
                var exception = false
                try {
                   date = sdf.parse("${innerData.date} ${innerData.time}")
                }catch (e: ParseException){
                    e.printStackTrace()
                    exception = true
                }
                val endTime = Date(date.time + (ONGOING_EVENT_TIME * 3600000))
                if (currTime.before(date) || currTime.after(endTime) || exception)
                    innerIterator.remove()
                else{
                    Log.e(TAG, "currTime")
                }
            }

            if (pair.second.isEmpty())
                iterator.remove()
        }

        fillCategoryWiseData(dateWiseOngoingEventsList, categoryWiseOngoingEventsList)
        Collections.sort(categoryWiseOngoingEventsList, outerCategoryComparator)
        innerSort("Time", categoryWiseOngoingEventsList)
    }

    fun updateListsWithFavourites(){
        for (i in dateWiseData)
            for (j in i.second)
                j.favouriteState = favourites.containsKey("${j.name}|${j.time}|${j.date}|${j.venue}")

        for (i in categoryWiseData)
            for (j in i.second)
                j.favouriteState = favourites.containsKey("${j.name}|${j.time}|${j.date}|${j.venue}")

        fillFavouriteList()
    }

    fun changeTimeFormatTo(timeFormat: Int){
        val time24 = SimpleDateFormat("HH:mm")
        val time12 = SimpleDateFormat("hh:mm a")
        if (timeFormat == 12)
            dateWiseData.forEach { i -> i.second.forEach { j ->  try { j.time = time12.format(time24.parse(j.time))}catch (e: ParseException){ e.printStackTrace()} } }
        else if (timeFormat == 24)
            dateWiseData.forEach { i -> i.second.forEach { j ->  try { j.time = time24.format(time12.parse(j.time))}catch (e: ParseException){ e.printStackTrace()} } }
    }

    fun init(){
        fillCategoryAndVenueFilters()
        Collections.sort(dateWiseData, outerDateComparator)
        Collections.sort(categoryWiseData, outerCategoryComparator)
        innerSort("Time", dateWiseData)
        innerSort("Time", categoryWiseData)
        fillFavouriteList()
        fillOngoingEventList()
        changeTimeFormatTo(tinyDb.getInt("timeFormat"))
    }

    fun getIconOfCategory(category: String) = when (category) {
        "Dance" -> android.R.drawable.ic_delete
        "Oratory" -> android.R.drawable.ic_delete
        "Misc" -> android.R.drawable.ic_delete
        "Fashion" -> android.R.drawable.ic_delete
        "Photography" -> android.R.drawable.ic_delete
        "Music" -> android.R.drawable.ic_delete
        "Drama" -> android.R.drawable.ic_delete
        "Fine Art" -> android.R.drawable.ic_delete
        "Online" -> android.R.drawable.ic_delete
        "Quizzing" -> android.R.drawable.ic_delete
        else -> android.R.drawable.ic_delete
    }
}