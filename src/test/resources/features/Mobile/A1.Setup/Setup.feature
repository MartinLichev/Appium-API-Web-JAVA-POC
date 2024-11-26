Feature: Display Currency and navigate to Main Page

    Scenario: User receives info about the currency and navigates to the main page
        Given the application is launched
        And the text "AndroMoney is a personal finance tool for use on your mobile phone" should be visible on the screen
        And I tap the element identified by "accessibilityid" with value "Navigate up"
        Then the element identified by "id" with value "com.kpmoney.android:id/decor_content_parent" should be visible



