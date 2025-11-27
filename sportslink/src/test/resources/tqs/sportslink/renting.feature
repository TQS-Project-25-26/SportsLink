# Feature: SL-25 Browse all available facilities
Scenario: Renter searches for padel courts in Aveiro
  Given I am logged in as a renter
  When I search for "Padel" in "Aveiro"
  Then I should see available padel courts
  And results should show location and price

# Feature: SL-27 Search by name, sport, type
Scenario: Renter searches by multiple criteria
  Given I am on the search page
  When I filter by sport "Futsal" and location "5km radius"
  Then I should see only futsal fields within 5km
  And results should be sorted by distance

# Feature: SL-26 Filtering & sorting
Scenario: Renter filters by price range
  Given search results show 10 facilities
  When I filter by price "<€25/hour"
  Then only facilities under €25 should display