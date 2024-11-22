Feature: Select Location and Navigate to Main Page

    Scenario: User selects their location to access the main page
        When I launch the app
        Then I should see the element with "text" locator "Takeaway"
        When I click the element with "text" locator "Takeaway"

# Then I should see the element with "id" locator "com.bgmenu.android:id/searchInput"
# When I enter "Sofia Airport" in the element with "id" locator "com.bgmenu.android:id/searchInput"
# Then I should see the element with "id" locator "com.bgmenu.android:id/nominatimMessage"
# When I click the element with "id" locator "com.bgmenu.android:id/nominatimMessage"

# Then I should see the element with "id" locator "com.bgmenu.android:id/restaurantListSearch"
