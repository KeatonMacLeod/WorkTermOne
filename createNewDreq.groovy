def createNewDreq(request) {

    def newDreq = null
    def newAuthority = null
    def newSignatory = null
    def businessActivity
    def businessSeqnCount = 1
    def currUser = springSecurityService.getCurrentUser()

    def validRequest = validateDreqRequest(null, request, false)
    if (validRequest == true) {
        try {

            Date createDate = new Date()

            /*************************************
             * Save the single DREQ to the Database
             *************************************/

            newDreq = new Dreq()

            newDreq.dreqTitle = request.JSON.dreqTitle
            newDreq.dreqDescription = request.JSON.dreqDescription
            newDreq.dreqType = DreqType.findById(request.JSON.dreqType)
            newDreq.dreqDocumentLink = request.JSON.dreqDocumentLink
            newDreq.dreqStatus = DreqStatusType.findById(1) //1 represents Draft
            newDreq.dreqIssueDate = null
            newDreq.logicalDeletedByPDSSUserId = null
            newDreq.logicalDeletedOnDatetime = null
            newDreq.createdByPDSSUserId = currUser.id
            newDreq.createdOnDatetime = createDate
            newDreq.lastUpdatedByPDSSUserId = currUser.id
            newDreq.lastUpdatedOnDatetime = createDate

            if (!newDreq.save(flush: true))
            {
                println newDreq.errors
            } 
            else 
            {
                //Write Business Activity
                newDreq.save(flush: true)

                businessActivity = businessActivityService.create(3, "User " + currUser.id + " (" + currUser.legalNames
                        + " " + currUser.lastName + ") created DR " + newDreq.id + " (" + newDreq.dreqTitle + ")")
                businessEntityService.create(currUser.id, businessSeqnCount++, 1, businessActivity.id)
                businessEntityService.create(newDreq.id, businessSeqnCount++, 2, businessActivity.id)

                /*******************************************
                 * Save multiple Authorities to the Database
                 *******************************************/

                def authoritiesList = request.JSON.authorities

                for (int authorityCount = 0; authorityCount < authoritiesList.size(); authorityCount++) {
                    newAuthority = new Authority()

                    newAuthority.slotTitle = DreqAuthorityType.findById(authoritiesList[authorityCount].slotTitle)
                    newAuthority.slotDreq = newDreq
                    newAuthority.slotSeq = authoritiesList[authorityCount].slotSeq
                    newAuthority.logicalDeletedByPDSSUserId = null
                    newAuthority.logicalDeletedOnDatetime = null
                    newAuthority.createdByPDSSUserId = currUser.id
                    newAuthority.createdOnDatetime = createDate
                    newAuthority.lastUpdatedByPDSSUserId = currUser.id
                    newAuthority.lastUpdatedOnDatetime = createDate

                    if (!newAuthority.save(flush: true)) 
                    {
                        println newAuthority.errors
                        throw new RuntimeException()
                    } 
                    else 
                    {
                        newAuthority.save(flush: true)
                        businessEntityService.create(newAuthority.id, businessSeqnCount++, 3, businessActivity.id)

                        /*******************************************
                         * Save multiple Signatories to the Database
                         *******************************************/

                        def signatoriesList = authoritiesList[authorityCount].signatories

                        for (int signatoryCount = 0; signatoryCount < signatoriesList.size(); signatoryCount++) {
                            newSignatory = new Signatory()

                            newSignatory.signatoryTitle = DreqTitleType.findById(signatoriesList[signatoryCount].signatoryTitle)
                            newSignatory.signatorySlot = newAuthority
                            newSignatory.sequence = signatoriesList[signatoryCount].sequence
                            newSignatory.signatoryPdss = signatoriesList[signatoryCount].signatoryPdss
                            newSignatory.logicalDeletedByPDSSUserId = null
                            newSignatory.logicalDeletedOnDatetime = null
                            newSignatory.createdByPDSSUserId = currUser.id
                            newSignatory.createdOnDatetime = createDate
                            newSignatory.lastUpdatedByPDSSUserId = currUser.id
                            newSignatory.lastUpdatedOnDatetime = createDate

                            if (!newSignatory.save(flush: true)) {
                                println newSignatory.errors
                                throw new RuntimeException()
                            } else {
                                //Write Business Activity
                                newSignatory.save(flush: true)
                                businessEntityService.create(newSignatory.id, businessSeqnCount++, 4, businessActivity.id)
                            }

                        }//signatory:for

                    }//authority:else

                }//authority:for

            }//dreq:else

        }//try

        catch (Exception e) {
            println e
            throw new RuntimeException()
        }//catch

        return newDreq
    } 
    else 
    {
        return validRequest
    }
}//createNewDreq
