Feature: UI and API Data Synchronization

  @Hybrid @TS-E2E-01
  Scenario Outline: Verify dynamic note created via UI is retrievable via GET API
    Given the user is logged into the Notes UI with valid credentials
    When the user creates a note via UI with title "<title>" description "<description>" and category "<category>"
    Then the GET "/notes" API should return the note with title "<title>"

    Examples:
      | title                        | description                      | category |
      | Sync Note Home - 5001        | Home note for sync test          | Home     |



  @Hybrid @TS-E2E-02
  Scenario Outline: Verify editing a note via UI is reflected in the API
    Given the user is logged into the Notes UI with valid credentials
    And a note exists via UI with title "<original_title>" description "<original_desc>" category "<category>"
    When the user edits that note with new title "<updated_title>" and description "<updated_desc>"
    Then the GET "/notes" API should return the note with title "<updated_title>"

    Examples:
      | original_title        | original_desc          | category | updated_title              | updated_desc           |
      | Edit Sync Note - 6001 | Original sync note     | Home     | Edit Sync Note UPDATED-6001| Updated sync content   |


  @Hybrid @TS-E2E-03
  Scenario Outline: Verify note created via API appears on the UI dashboard
    Given the user is logged into the Notes UI with valid credentials
    When a note is created via the API with title "<title>" description "<description>" and category "<category>"
    And the user refreshes the UI dashboard
    Then the API created note with title "<title>" should be visible on the UI dashboard

    Examples:
      | title                    | description                    | category |
      | API Created Note - 7001  | Created via API for UI check   | Personal |


  @Hybrid @TS-E2E-04
  Scenario Outline: Verify note deleted via API disappears from the UI dashboard
    Given the user is logged into the Notes UI with valid credentials
    And a note is created via the API with title "<title>" description "<description>" and category "<category>"
    And the user refreshes the UI dashboard
    And the API created note with title "<title>" should be visible on the UI dashboard
    When the note with title "<title>" is deleted via the API
    And the user refreshes the UI dashboard
    Then the note with title "<title>" should no longer be visible on the UI dashboard

    Examples:
      | title                      | description                        | category |
      | API Delete Sync - 8001     | Note to be deleted via API sync    | Home     |


