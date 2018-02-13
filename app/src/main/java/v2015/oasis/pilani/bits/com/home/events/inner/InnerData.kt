package v2015.oasis.pilani.bits.com.home.events.inner

import java.io.Serializable


data class InnerData(val name: String,
                     val category: String,
                     val categoryIcon: Int,
                     val description: String,
                     val rules: String,
                     var time: String,
                     val date: String,
                     val venue: String,
                     var favouriteState: Boolean) : Serializable