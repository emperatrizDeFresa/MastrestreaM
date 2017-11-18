package emperatriz.riverflood.model

interface GestorPagina {

    val nombre: String
    val id: Int
    fun parseData()
    fun parseDataRefresh()
    fun getLinks(index: Int)
    fun updateUrl(url:String)


}
