import unittest
from unittest.mock import MagicMock
import sys

# Mock modules before import
mock_db_module = MagicMock()
mock_firestore_module = MagicMock()
sys.modules["config"] = MagicMock()
sys.modules["config.db"] = mock_db_module
sys.modules["firebase_admin"] = MagicMock()
sys.modules["firebase_admin.firestore"] = mock_firestore_module

# Setup mocks
mock_db = MagicMock()
mock_db_module.db = mock_db
mock_firestore_module.SERVER_TIMESTAMP = "SERVER_TIMESTAMP"

# Now import the module under test
from workouts_helpers import process_workout_exercises

class TestWorkoutsHelpersPerf(unittest.TestCase):
    def setUp(self):
        # Reset the mock DB and its children before each test
        mock_db.reset_mock()
        # We need to ensure a clean slate for return values if we modify them
        # Re-create the chain for each test to avoid cross-contamination
        self.mock_users_coll = MagicMock()
        mock_db.collection.return_value = self.mock_users_coll
        self.mock_user_doc = MagicMock()
        self.mock_users_coll.document.return_value = self.mock_user_doc
        self.mock_prs_coll = MagicMock()
        self.mock_user_doc.collection.return_value = self.mock_prs_coll
        self.mock_pr_doc_ref = MagicMock()
        self.mock_prs_coll.document.return_value = self.mock_pr_doc_ref

    def test_process_workout_exercises_perf(self):
        user_id = "test_user"
        workout_id = "test_workout"
        exercises_data = [
            {
                "exerciseId": "ex1",
                "sets": [
                    {"weight": 100, "reps": 5, "isPR": False},
                    {"weight": 105, "reps": 3, "isPR": True},
                    {"weight": 90, "reps": 10, "isPR": False}
                ]
            },
            {
                "exerciseId": "ex2",
                "sets": [
                    {"weight": 50, "reps": 10, "isPR": False},
                    {"weight": 50, "reps": 10, "isPR": False},
                    {"weight": 55, "reps": 8, "isPR": False}
                ]
            }
        ]

        # Setup Mock for success
        mock_pr_doc = MagicMock()
        mock_pr_doc.exists = True
        mock_pr_doc.to_dict.return_value = {"weight": 95, "reps": 5}
        self.mock_pr_doc_ref.get.return_value = mock_pr_doc

        # Run the function
        process_workout_exercises(user_id, workout_id, exercises_data)

        # Verify call count
        get_call_count = self.mock_pr_doc_ref.get.call_count
        print(f"DEBUG: get() called {get_call_count} times")
        self.assertEqual(get_call_count, 2, f"Expected 2 DB reads (1 per exercise), but got {get_call_count}")

        # Verify write count
        set_call_count = self.mock_pr_doc_ref.set.call_count
        print(f"DEBUG: set() called {set_call_count} times")
        self.assertEqual(set_call_count, 1, f"Expected 1 DB write, but got {set_call_count}")

    def test_process_workout_exercises_fetch_failure(self):
        user_id = "test_user_fail"
        workout_id = "test_workout_fail"
        exercises_data = [
            {
                "exerciseId": "ex_fail",
                "sets": [{"weight": 200, "reps": 1, "isPR": False}]
            }
        ]

        # Setup Mock to raise Exception
        self.mock_pr_doc_ref.get.side_effect = Exception("Firestore Error")

        # Run function
        process_workout_exercises(user_id, workout_id, exercises_data)

        # Verify no writes
        set_call_count = self.mock_pr_doc_ref.set.call_count
        print(f"DEBUG: set() called {set_call_count} times on failure")
        self.assertEqual(set_call_count, 0, "Expected 0 writes when fetch fails")

if __name__ == '__main__':
    unittest.main()
