package com.example

data class PageContent(
    val pageIndex: Int, // 0-based
    val totalPages: Int,
    val sections: List<WorksheetSection>,
    val isFirstPage: Boolean,
    val isLastPage: Boolean,
    val hasNotesBox: Boolean
)

object WorksheetPaginator {
    // Relative capacity units matching standard A4 dimensions (595 x 842 points)
    const val PAGE1_CONTENT_CAPACITY = 440
    const val SUBSEQUENT_PAGE_CAPACITY = 640

    fun estimateSectionHeight(sec: WorksheetSection): Int {
        val titleLines = 1 + (sec.title.length / 45)
        val contentLines = sec.content.lines().sumOf { line -> 1 + (line.length / 65) }
        return 34 * titleLines + 20 * contentLines + 24
    }

    fun paginate(data: WorksheetData): List<PageContent> {
        if (data.sections.isEmpty()) {
            return listOf(
                PageContent(
                    pageIndex = 0,
                    totalPages = 1,
                    sections = emptyList(),
                    isFirstPage = true,
                    isLastPage = true,
                    hasNotesBox = true
                )
            )
        }

        val pages = mutableListOf<MutableList<WorksheetSection>>()
        var currentPage = mutableListOf<WorksheetSection>()
        var currentUsed = 0
        var isPage1 = true

        for (section in data.sections) {
            val h = estimateSectionHeight(section)
            val capacity = if (isPage1) PAGE1_CONTENT_CAPACITY else SUBSEQUENT_PAGE_CAPACITY

            if (currentPage.isNotEmpty() && (currentUsed + h > capacity)) {
                pages.add(currentPage)
                currentPage = mutableListOf(section)
                currentUsed = h
                isPage1 = false
            } else {
                currentPage.add(section)
                currentUsed += h
            }
        }
        if (currentPage.isNotEmpty() || pages.isEmpty()) {
            pages.add(currentPage)
        }

        val total = pages.size
        return pages.mapIndexed { index, secs ->
            val isFirst = index == 0
            val isLast = index == total - 1
            val used = secs.sumOf { estimateSectionHeight(it) }
            val remaining = (if (isFirst) PAGE1_CONTENT_CAPACITY else SUBSEQUENT_PAGE_CAPACITY) - used
            val hasNotes = isLast && remaining > 90
            PageContent(
                pageIndex = index,
                totalPages = total,
                sections = secs,
                isFirstPage = isFirst,
                isLastPage = isLast,
                hasNotesBox = hasNotes
            )
        }
    }
}
