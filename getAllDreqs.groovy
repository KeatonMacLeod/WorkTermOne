def getAllDreqs(params) {

    def dreqs = null
    def toDataTable = null
    def jsonArray = []

    def searchString = params."search[value]"
    def searchDate = getDateSearchString(searchString)
    searchString = "%" + searchString.toUpperCase() + "%"

    if (params.statuses != null && params.types != null) {
        
        def queryString = "Select d from Dreq as d"

        String dreqStatusString = params.statuses //The variables are strings but
        String dreqTypeString = params.types //look like arrays eg.) [1,2,4]

        //Take off the beginning and end brackets "[" and "]"
        dreqStatusString = dreqStatusString.substring(1, dreqStatusString.length() - 1)
        dreqTypeString = dreqTypeString.substring(1, dreqTypeString.length() - 1)

        //Put the id's of the statuses and types into these arrays
        String[] dreqStatusArray = dreqStatusString.split(",")
        String[] dreqTypeArray = dreqTypeString.split(",")

        /************************************************************
         * When the criteria contains no dreqTypes or no dreqStatuses
         ************************************************************/
        if (dreqTypeString.length() <= 0 || dreqStatusString.length() <= 0) {
            queryString += " where 0 = 1"
        }//if

        /************************************************************
         * When the criteria contains both dreqTypes and dreqStatuses
         ************************************************************/
        else {
            queryString += " where (d.dreqType.id = '"

            for (int i = 0; i < dreqTypeArray.length; i++) {
                queryString += "${dreqTypeArray[i]}"
                if (i + 1 < dreqTypeArray.length) {
                    queryString += "' or d.dreqType.id = '"
                }
            }

            queryString += "') and (d.dreqStatus.id = '"

            for (int j = 0; j < dreqStatusArray.length; j++) {
                queryString += "${dreqStatusArray[j]}"
                if (j + 1 < dreqStatusArray.length) {
                    queryString += "' or d.dreqStatus.id = '"
                }
            }

            queryString += "')"

        }//else if

        if (searchString != "%%") {
            queryString += "and (upper(d.id) like '" + searchString + "' \
            or upper(d.dreqTitle) like '" + searchString + "' \
            or upper(d.dreqType.dreqTypeNameEnglish) like '" + searchString + "' \
            or upper(d.dreqType.dreqTypeNameFrench) like '" + searchString + "' \
            or upper(d.dreqStatus.dreqStatusTypeNameEnglish) like '" + searchString + "' \
            or upper(d.dreqStatus.dreqStatusTypeNameFrench) like '" + searchString + "' \
            or year(d.lastUpdatedOnDatetime) in " + searchDate + " \
            or month(d.lastUpdatedOnDatetime) in " + searchDate + " \
            or day(d.lastUpdatedOnDatetime) in " + searchDate + " \
            or year(d.createdOnDatetime) in " + searchDate + " \
            or month(d.createdOnDatetime) in " + searchDate + " \
            or day(d.createdOnDatetime) in " + searchDate + " \
            or upper(d.dreqDocumentLink) like '" + searchString + "')"
        }
        queryString += " order by d.lastUpdatedOnDatetime desc"

        dreqs = Dreq.executeQuery(queryString)
        def dreqCount = dreqs.size();

        dreqs = dreqs.drop(Integer.parseInt(params.start))
        dreqs = dreqs.take(Integer.parseInt(params.length))

        for (int i = 0; i < dreqs.size(); i++) {
            jsonArray.push(new JSONObject([
                    "id"                       : dreqs[i].id,
                    "dreqTitle"                : dreqs[i].dreqTitle,
                    "dreqTypeNameEnglish"      : dreqs[i].dreqType.dreqTypeNameEnglish,
                    "dreqTypeNameFrench"       : dreqs[i].dreqType.dreqTypeNameFrench,
                    "dreqStatusTypeNameEnglish": dreqs[i].dreqStatus.dreqStatusTypeNameEnglish,
                    "dreqStatusTypeNameFrench" : dreqs[i].dreqStatus.dreqStatusTypeNameFrench,
                    "lastUpdatedOnDatetime"    : dreqs[i].lastUpdatedOnDatetime,
                    "createdOnDatetime"        : dreqs[i].createdOnDatetime,
                    "dreqDocumentLink"         : dreqs[i].dreqDocumentLink
            ]))
        }

        toDataTable = new JSONObject([
                "draw:"          : params.draw,
                "recordsTotal"   : Dreq.count(),
                "recordsFiltered": dreqCount,
                "data"           : jsonArray
        ])
    }

    return toDataTable

}//getAllDreqs
