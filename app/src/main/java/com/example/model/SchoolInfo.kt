package com.example.model

data class SchoolInfo(
    val officeCode: String = "C10",
    val officeName: String = "부산광역시교육청",
    val schoolCode: String = "7150597",
    val schoolName: String = "대진전자통신고등학교",
    val engSchoolName: String = "Daejin High School of Electronics & Communication",
    val schoolType: String = "고등학교",
    val locationName: String = "부산광역시",
    val address: String = "부산광역시 금정구 수림로 92 (장전동)",
    val phone: String = "051-582-8100",
    val website: String = "www.pdj.hs.kr",
    val fax: String = "051-582-8120",
    val foundingDate: String = "1995-10-30"
) {
    companion object {
        val DAEJIN_HIGH_SCHOOL = SchoolInfo()
    }
}
