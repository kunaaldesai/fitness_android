import sys
from unittest.mock import MagicMock

# --- Mocks for Flask ---
mock_flask_module = MagicMock()
mock_app = MagicMock()
mock_flask_module.Flask.return_value = mock_app

# Mock request
mock_request = MagicMock()
mock_flask_module.request = mock_request

# Mock jsonify
def mock_jsonify(data):
    return data # Just return the data for inspection
mock_flask_module.jsonify = mock_jsonify

# Mock g
mock_g = MagicMock()
mock_flask_module.g = mock_g

sys.modules['flask'] = mock_flask_module

# --- Mocks for Other modules ---
mock_db = MagicMock()
mock_firestore = MagicMock()

# Mock config.db
mock_config = MagicMock()
mock_config.db = MagicMock()
mock_config.db.db = mock_db
sys.modules['config'] = mock_config
sys.modules['config.db'] = mock_config.db

# Mock firebase_admin
mock_firebase = MagicMock()
mock_firebase_firestore = MagicMock()
# SERVER_TIMESTAMP mock
mock_firebase_firestore.SERVER_TIMESTAMP = "SERVER_TIMESTAMP"
sys.modules['firebase_admin'] = mock_firebase
sys.modules['firebase_admin.firestore'] = mock_firebase_firestore

# Mock pytz
sys.modules['pytz'] = MagicMock()

# --- Dummy ERROR_CODES ---
ERROR_CODES = {
    "NO_DATA_PROVIDED": {"message": "No data provided.", "code": 400},
    "INVALID_REQUEST": {"message": "Invalid request.", "code": 400},
    "USER_CREATION_FAILED": {"message": "User creation failed.", "code": 500},
    "USER_UPDATE_FAILED": {"message": "User update failed.", "code": 500},
    "USER_NOT_FOUND": {"message": "User not found.", "code": 404},
    "INTERNAL_SERVER_ERROR": {"message": "Internal server error.", "code": 500},
    "USER_DELETE_FAILED": {"message": "User delete failed.", "code": 500},
}

# --- Load users.py ---
with open("users.py", "r") as f:
    code = f.read()

# Replace import
code = code.replace("from .error_codes import ERROR_CODES", "")
# Inject ERROR_CODES
code = f"ERROR_CODES = {ERROR_CODES}\n" + code

# Exec
global_vars = {
    "Flask": mock_flask_module.Flask,
    "request": mock_request,
    "jsonify": mock_jsonify,
    "g": mock_g,
    "db": mock_db,
    "firestore": mock_firebase_firestore,
}

exec(code, global_vars)

# Extract functions
# Since we mocked Flask, @app.route is just a mock call.
# We need to find the decorated functions.
# The `create_users_app` function returns the app.
# Inside `create_users_app`, the functions `getUser`, `createUser` etc are defined.
# We need to access them.
# The standard `create_users_app` defines inner functions and decorates them.
# But since `app.route` is a mock, it returns a decorator mock.
# The decorator mock, when called with the function, returns... the function?
# By default, MagicMock return value is another MagicMock.
# So `createUser` will be replaced by a MagicMock if we are not careful.
# We need `app.route` side_effect to return the function itself (identity).

def route_side_effect(rule, **options):
    def decorator(f):
        # Store the function in the mock app for easy access
        if not hasattr(mock_app, 'routes'):
            mock_app.routes = {}
        mock_app.routes[rule] = f
        return f
    return decorator

mock_app.route.side_effect = route_side_effect

# Initialize routes dict explicitly
mock_app.routes = {}

# Re-run exec to apply the side effect
create_users_app = global_vars['create_users_app']
app_instance = create_users_app()
print(f"DEBUG: app_instance is mock_app? {app_instance is mock_app}")
print(f"DEBUG: app_instance.routes type: {type(app_instance.routes)}")
print(f"DEBUG: app_instance.routes keys: {app_instance.routes.keys()}")
# Now app_instance.routes should contain the functions mapping

# Helper to invoke route handler
def invoke_route(rule, method, json_data=None):
    handler = app_instance.routes.get(rule)
    if not handler:
        # Try finding by prefix for dynamic routes if needed
        # But here we test createUser (static) and updateUser/<id> (dynamic)
        # The keys in routes will be "/createUser", "/updateUser/<id>"
        # For dynamic routes, we need to match the pattern or just access by the definition string.
        pass

    # Setup request mock
    mock_request.get_json.return_value = json_data
    mock_request.method = method

    # Invoke
    # For dynamic routes like /updateUser/<id>, the handler expects arguments.
    # We'll need to pass them.
    return handler

def test_createUser_capitalization_bug():
    print("\n--- Testing createUser Capitalization Bug ---")
    data = {
        "id": "test_user_1",
        "firstName": "john",
        "lastName": "doe",
        "email": "john.doe@example.com"
    }

    handler = app_instance.routes['/createUser']

    mock_db.collection.return_value.document.return_value.create.reset_mock()

    # Call handler
    mock_request.get_json.return_value = data
    result = handler()
    print(f"DEBUG: handler returned: {result}")
    response, status = result

    if status != 200:
        print(f"FAILED: Expected 200, got {status}")
        return

    # Check arguments passed to create()
    args, _ = mock_db.collection.return_value.document.return_value.create.call_args
    created_data = args[0]

    print(f"Data passed to create(): {created_data}")

    if created_data["firstName"] == "John" and created_data["lastName"] == "Doe":
        print("SUCCESS: Names are capitalized!")
    else:
        print(f"FAILED: firstName={created_data['firstName']}, lastName={created_data['lastName']}")

def test_createUser_long_input():
    print("\n--- Testing createUser Long Input ---")
    long_string = "a" * 1000
    data = {
        "id": "test_user_2",
        "firstName": long_string,
        "lastName": "doe",
        "bio": long_string
    }

    handler = app_instance.routes['/createUser']

    mock_request.get_json.return_value = data
    response, status = handler()

    print(f"Status Code: {status}")
    if status == 400:
        print("SUCCESS: Request rejected as expected.")
    else:
        print(f"FAILED: Expected 400, got {status}")

def test_updateUser_capitalization_bug():
    print("\n--- Testing updateUser Capitalization Bug ---")
    user_id = "test_user_3"
    data = {
        "firstName": "jane",
        "lastName": "smith"
    }

    handler = app_instance.routes['/updateUser/<id>']

    # Mock exists
    mock_db.collection.return_value.document.return_value.get.return_value.exists = True
    mock_db.collection.return_value.document.return_value.update.reset_mock()

    mock_request.get_json.return_value = data
    response, status = handler(user_id)

    if status != 200:
        print(f"FAILED: Expected 200, got {status}")
        return

    # Check arguments passed to update()
    args, _ = mock_db.collection.return_value.document.return_value.update.call_args
    updated_data = args[0]

    print(f"Data passed to update(): {updated_data}")

    if updated_data["firstName"] == "Jane" and updated_data["lastName"] == "Smith":
        print("SUCCESS: Names are capitalized!")
    else:
        print(f"FAILED: firstName={updated_data.get('firstName')}, lastName={updated_data.get('lastName')}")

if __name__ == "__main__":
    test_createUser_capitalization_bug()
    test_createUser_long_input()
    test_updateUser_capitalization_bug()
