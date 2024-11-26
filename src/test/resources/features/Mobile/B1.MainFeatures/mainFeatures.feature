Feature: Main Features

    Scenario: Right Panel Account Info is visible when user clicks accordion button
        When I tap the element identified by "accessibilityid" with value "AndroMoney"
        Then the element identified by "text" with value "Search" should be visible
        Then the element identified by "text" with value "Account" should be visible
        Then the element identified by "text" with value "Budget" should be visible
        Then the element identified by "text" with value "Category" should be visible
        Then the element identified by "text" with value "Report" should be visible
        Then the element identified by "text" with value "Share" should be visible
        Then the element identified by "text" with value "Comment" should be visible

    Scenario: Search by Account
        When I tap the element identified by "text" with value "Search"
        Then the element identified by "text" with value "Keywords" should be visible
        And I tap the element identified by "id" with value "com.kpmoney.android:id/payment_layout"
        Then the element identified by "id" with value "com.kpmoney.android:id/alertTitle" should be visible
        And I tap the element identified by "id" with value "com.kpmoney.android:id/account_filter_dialog_cb"
        Then the element identified by "attribute" with value "checked=false" should be visible
        And I tap the element identified by "text" with value "Bank"
        And I tap the element identified by "text" with value "OK"
        Then the element identified by "text" with value "Bank" should be visible