import sys
import os
import shutil
import unittest
from unittest.mock import MagicMock, patch

"""
Security Test Suite for users.py

Note: This test suite uses extensive mocking and source code modification
to enable testing of users.py in an environment where dependencies (Flask,
Firebase Admin, etc.) and project structure (config package, error_codes.py)
are missing or not installable.

It creates a temporary 'users_fixed.py' to resolve relative imports which
fail when running users.py as a standalone script.
"""

# Mock flask components BEFORE importing
mock_flask = MagicMock()
mock_app_instance = MagicMock()
mock_app_instance.view_functions = {}

def mock_route(rule, **options):
    def decorator(f):
        mock_app_instance.view_functions[rule] = f
        return f
    return decorator

mock_app_instance.route = MagicMock(side_effect=mock_route)
mock_flask.Flask.return_value = mock_app_instance

mock_request = MagicMock()
mock_jsonify = MagicMock(side_effect=lambda x: x) # Return the dict as is
mock_g = MagicMock()

mock_flask.request = mock_request
mock_flask.jsonify = mock_jsonify
mock_flask.g = mock_g

sys.modules['flask'] = mock_flask

# Mock config.db
mock_db_obj = MagicMock()
mock_config = MagicMock()
mock_config_db = MagicMock()
mock_config_db.db = mock_db_obj
sys.modules['config'] = mock_config
sys.modules['config.db'] = mock_config_db

# Mock firebase_admin
sys.modules['firebase_admin'] = MagicMock()
sys.modules['firebase_admin.firestore'] = MagicMock()

# Mock pytz
sys.modules['pytz'] = MagicMock()

# Create a dummy error_codes.py
with open("error_codes.py", "w") as f:
    f.write('ERROR_CODES = {"INVALID_REQUEST": {"code": 400, "message": "Invalid Request"}, "NO_DATA_PROVIDED": {"code": 400, "message": "No data provided"}, "USER_NOT_FOUND": {"code": 404, "message": "User not found"}, "INTERNAL_SERVER_ERROR": {"code": 500, "message": "Internal Server Error"}, "USER_UPDATE_FAILED": {"code": 500, "message": "Update failed"}, "USER_CREATION_FAILED": {"code": 500, "message": "Creation failed"}, "USER_DELETE_FAILED": {"code": 500, "message": "Delete failed"}}')

# Create a copy of users.py with fixed import
if os.path.exists("users.py"):
    with open("users.py", "r") as f:
        content = f.read()

    # Replace relative import with absolute
    content = content.replace("from .error_codes import ERROR_CODES", "from error_codes import ERROR_CODES")

    with open("users_fixed.py", "w") as f:
        f.write(content)
else:
    print("users.py not found!")
    sys.exit(1)

# Now import the fixed users file
import users_fixed as users

class TestUsersSecurity(unittest.TestCase):
    def setUp(self):
        # Reset mocks
        mock_request.reset_mock()
        mock_db_obj.reset_mock()
        # Initialize app to populate view_functions
        self.app = users.create_users_app()
        self.functions = self.app.view_functions

    def test_create_user_validation_firstname(self):
        long_name = "A" * 51
        mock_request.get_json.return_value = {
            "id": "test_uid",
            "firstName": long_name,
            "lastName": "Doe"
        }

        # /createUser maps to createUser function
        create_user_func = self.functions['/createUser']
        response, status_code = create_user_func()

        self.assertEqual(status_code, 400)
        self.assertIn("firstName must be a string", response['details'])

    def test_create_user_validation_bio(self):
        long_bio = "B" * 501
        mock_request.get_json.return_value = {
            "id": "test_uid",
            "firstName": "John",
            "bio": long_bio
        }

        create_user_func = self.functions['/createUser']
        response, status_code = create_user_func()

        self.assertEqual(status_code, 400)
        self.assertIn("bio must be a string", response['details'])

    def test_create_user_validation_types(self):
        mock_request.get_json.return_value = {
            "id": "test_uid",
            "firstName": 12345
        }

        create_user_func = self.functions['/createUser']
        response, status_code = create_user_func()

        self.assertEqual(status_code, 400)
        self.assertIn("firstName must be a string", response['details'])

    def test_update_user_validation(self):
        # Mock DB get to return existing user
        mock_doc = MagicMock()
        mock_doc.exists = True
        mock_db_obj.collection.return_value.document.return_value.get.return_value = mock_doc

        long_bio = "B" * 501
        mock_request.get_json.return_value = {
            "bio": long_bio
        }

        # /updateUser/<id> maps to updateUser function
        # The function expects 'id' argument
        update_user_func = self.functions['/updateUser/<id>']
        response, status_code = update_user_func("test_uid")

        self.assertEqual(status_code, 400)
        self.assertIn("bio must be a string", response['details'])

    @classmethod
    def tearDownClass(cls):
        # Cleanup
        if os.path.exists("users_fixed.py"):
            os.remove("users_fixed.py")
        if os.path.exists("error_codes.py"):
            os.remove("error_codes.py")
        # Remove pycache if created
        if os.path.exists("__pycache__"):
            shutil.rmtree("__pycache__")

if __name__ == '__main__':
    unittest.main()
