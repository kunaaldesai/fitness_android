from flask import Flask, request, jsonify, g
from config.db import db
from .error_codes import ERROR_CODES
from firebase_admin import firestore
from datetime import datetime, timedelta
import pytz
import json
import logging

MAX_NAME_LENGTH = 50
MAX_BIO_LENGTH = 500
MAX_URL_LENGTH = 2048


def create_users_app():
    # Initialize Flask app
    usersApp = Flask(__name__)

    # Firestore - getUser by ID
    @usersApp.route('/getUser/<id>', methods=['GET'])
    def getUser(id):
        try:
            doc = db.collection('users').document(id).get()
            if not doc.exists:
                return jsonify({
                    "error": ERROR_CODES["USER_NOT_FOUND"]["message"],
                    "code": ERROR_CODES["USER_NOT_FOUND"]["code"],
                    "details": f"User {id} not found"
                }), 404
            user = doc.to_dict()
            user["id"] = id
            return jsonify(user), 200
        except Exception as e:
            logging.error(f"Could not retrieve user {id}: {e}")
            return jsonify({
                "error": ERROR_CODES["INTERNAL_SERVER_ERROR"]["message"],
                "code": ERROR_CODES["INTERNAL_SERVER_ERROR"]["code"],
                "details": f"Could not retrieve user {id}"
            }), 500

    # Firestore - getUsers
    @usersApp.route('/getUsers', methods=['GET'])
    def getUsers():
        try:
            docData = []
            userCollection = db.collection('users').stream()
            for doc in userCollection:
                # Include the document ID in the response
                user = doc.to_dict()
                user["id"] = doc.id
                docData.append(user)
            return jsonify(docData), 200
        except Exception as e:
            logging.error(f"Could not retrieve users: {e}")
            return jsonify({
                "error": ERROR_CODES["INTERNAL_SERVER_ERROR"]["message"],
                "code": ERROR_CODES["INTERNAL_SERVER_ERROR"]["code"],
                "details": "Could not retrieve users"
            }), 500

    @usersApp.route('/checkUserByPhone', methods=['POST'])
    def checkUserByPhone():
        try:
            data = request.get_json(silent=True) or {}
            phone = (data.get("phoneNumber") or data.get("phone") or "").strip()
            if not phone:
                return jsonify({
                    "error": ERROR_CODES["INVALID_REQUEST"]["message"],
                    "code": ERROR_CODES["INVALID_REQUEST"]["code"],
                    "details": "phoneNumber is required."
                }), 400

            query = db.collection("users").where("phoneNumber", "==", phone).limit(1)
            user_exists = any(True for _ in query.stream())

            return jsonify({
                "exists": user_exists
            }), 200
        except Exception as e:
            logging.error(f"Could not check phone number: {e}")
            return jsonify({
                "error": ERROR_CODES["INTERNAL_SERVER_ERROR"]["message"],
                "code": ERROR_CODES["INTERNAL_SERVER_ERROR"]["code"],
                "details": "Could not check phone number"
            }), 500

    # Firestore - createUser
    @usersApp.route('/createUser', methods=['POST'])
    def createUser():
        try:
            data = request.get_json(silent=True)
            if not data:
                return jsonify({
                    "error": ERROR_CODES["NO_DATA_PROVIDED"]["message"],
                    "code": ERROR_CODES["NO_DATA_PROVIDED"]["code"],
                    "details": "No data provided."
                }), 400
            
            # Add timestamps
            data["createdAt"] = firestore.SERVER_TIMESTAMP
            data["updatedAt"] = firestore.SERVER_TIMESTAMP

            # sanitize firstName and lastName
            first_name = data.get("firstName", "")
            last_name = data.get("lastName", "")

            if not isinstance(first_name, str) or len(first_name) > MAX_NAME_LENGTH:
                return jsonify({
                    "error": ERROR_CODES["INVALID_REQUEST"]["message"],
                    "code": ERROR_CODES["INVALID_REQUEST"]["code"],
                    "details": f"firstName must be a string under {MAX_NAME_LENGTH} characters."
                }), 400

            if not isinstance(last_name, str) or len(last_name) > MAX_NAME_LENGTH:
                return jsonify({
                    "error": ERROR_CODES["INVALID_REQUEST"]["message"],
                    "code": ERROR_CODES["INVALID_REQUEST"]["code"],
                    "details": f"lastName must be a string under {MAX_NAME_LENGTH} characters."
                }), 400

            if first_name:
                first_name.capitalize()
                data["firstName"] = first_name
            if last_name:
                last_name.capitalize()
                data["lastName"] = last_name
            
            #additional user data not asked during onboarding
            bio = data.get("bio", "")
            image_url = data.get("imageUrl", "")

            if not isinstance(bio, str) or len(bio) > MAX_BIO_LENGTH:
                return jsonify({
                    "error": ERROR_CODES["INVALID_REQUEST"]["message"],
                    "code": ERROR_CODES["INVALID_REQUEST"]["code"],
                    "details": f"bio must be a string under {MAX_BIO_LENGTH} characters."
                }), 400

            if not isinstance(image_url, str) or len(image_url) > MAX_URL_LENGTH:
                return jsonify({
                    "error": ERROR_CODES["INVALID_REQUEST"]["message"],
                    "code": ERROR_CODES["INVALID_REQUEST"]["code"],
                    "details": f"imageUrl must be a string under {MAX_URL_LENGTH} characters."
                }), 400

            data["bio"] = bio
            data["imageUrl"] = image_url
            # Security: Prevent privilege escalation
            data["isAdmin"] = False
            if data.get("gender") is None:
                data["gender"] = "N/A"

            uid = data["id"]
            db.collection('users').document(uid).create(data)

            return jsonify({
                "message": "User created",
                "uid": uid
            }), 200
        except Exception as e:
            logging.error(f"Could not create user: {e}")
            return jsonify({
                "error": ERROR_CODES["USER_CREATION_FAILED"]["message"],
                "code": ERROR_CODES["USER_CREATION_FAILED"]["code"],
                "details": "Could not create user"
            }), 500

    # Firestore - deleteUser by ID
    @usersApp.route('/deleteUser/<id>', methods=['DELETE'])
    def deleteUser(id):
        try:
            doc = db.collection('users').document(id).get()
            if doc.exists:
                db.collection('users').document(id).delete() # for some reason it won't work with doc.delete()
                return jsonify({"message": f"User {id} deleted"}), 200
            else:
                return jsonify({
                    "error": ERROR_CODES["USER_NOT_FOUND"]["message"],
                    "code": ERROR_CODES["USER_NOT_FOUND"]["code"],
                    "details": f"User {id} not found"
                }), 404
        except Exception as e:
            logging.error(f"Could not delete user {id}: {e}")
            return jsonify({
                "error": ERROR_CODES["USER_DELETE_FAILED"]["message"],
                "code": ERROR_CODES["USER_DELETE_FAILED"]["code"],
                "details": f"Could not delete user {id}"
            }), 500

    # Firestore - updateUser by ID
    @usersApp.route('/updateUser/<id>', methods=['PUT'])
    def updateUser(id):
        try:
            data = request.get_json(silent=True)
            if not data:
                return jsonify({
                    "error": ERROR_CODES["NO_DATA_PROVIDED"]["message"],
                    "code": ERROR_CODES["NO_DATA_PROVIDED"]["code"],
                    "details": "No data provided."
                }), 400
            doc = db.collection('users').document(id).get()
            if doc.exists:
                # Security: Prevent privilege escalation
                data.pop("isAdmin", None)

                # Add updatedAt timestamp
                data["updatedAt"] = firestore.SERVER_TIMESTAMP

                first_name = data.get("firstName", "")
                last_name = data.get("lastName", "")
                bio = data.get("bio", "")
                image_url = data.get("imageUrl", "")

                if not isinstance(first_name, str) or len(first_name) > MAX_NAME_LENGTH:
                    return jsonify({
                        "error": ERROR_CODES["INVALID_REQUEST"]["message"],
                        "code": ERROR_CODES["INVALID_REQUEST"]["code"],
                        "details": f"firstName must be a string under {MAX_NAME_LENGTH} characters."
                    }), 400

                if not isinstance(last_name, str) or len(last_name) > MAX_NAME_LENGTH:
                    return jsonify({
                        "error": ERROR_CODES["INVALID_REQUEST"]["message"],
                        "code": ERROR_CODES["INVALID_REQUEST"]["code"],
                        "details": f"lastName must be a string under {MAX_NAME_LENGTH} characters."
                    }), 400

                if not isinstance(bio, str) or len(bio) > MAX_BIO_LENGTH:
                    return jsonify({
                        "error": ERROR_CODES["INVALID_REQUEST"]["message"],
                        "code": ERROR_CODES["INVALID_REQUEST"]["code"],
                        "details": f"bio must be a string under {MAX_BIO_LENGTH} characters."
                    }), 400

                if not isinstance(image_url, str) or len(image_url) > MAX_URL_LENGTH:
                    return jsonify({
                        "error": ERROR_CODES["INVALID_REQUEST"]["message"],
                        "code": ERROR_CODES["INVALID_REQUEST"]["code"],
                        "details": f"imageUrl must be a string under {MAX_URL_LENGTH} characters."
                    }), 400

                if first_name:
                    first_name.capitalize()
                    data["firstName"] = first_name
                if last_name:
                    last_name.capitalize()
                    data["lastName"] = last_name

                db.collection('users').document(id).update(data)
                return jsonify({"message": f"User {id} updated"}), 200
            else:
                return jsonify({
                    "error": ERROR_CODES["USER_NOT_FOUND"]["message"],
                    "code": ERROR_CODES["USER_NOT_FOUND"]["code"],
                    "details": f"User {id} not found"
                }), 404
        except Exception as e:
            logging.error(f"Could not update user {id}: {e}")
            return jsonify({
                "error": ERROR_CODES["USER_UPDATE_FAILED"]["message"],
                "code": ERROR_CODES["USER_UPDATE_FAILED"]["code"],
                "details": f"Could not update user {id}"
            }), 500
        
    # Firestore - getUser by ID
    @usersApp.route('/getUserV2/<id>', methods=['GET'])
    def getUserV2(id):
        try:
            doc = db.collection('users').document(id).get()
            if not doc.exists:
                return jsonify({
                    "error": ERROR_CODES["USER_NOT_FOUND"]["message"],
                    "code": ERROR_CODES["USER_NOT_FOUND"]["code"],
                    "details": f"User {id} not found"
                }), 404
            user = doc.to_dict()
            user["id"] = id
            viewer_id = request.args.get("viewerId")
            if not viewer_id:
                viewer_id = getattr(g, "user", {}).get("uid") if hasattr(g, "user") else None
            return jsonify(user), 200
        except Exception as e:
            logging.error(f"Could not retrieve user {id}: {e}")
            return jsonify({
                "error": ERROR_CODES["INTERNAL_SERVER_ERROR"]["message"],
                "code": ERROR_CODES["INTERNAL_SERVER_ERROR"]["code"],
                "details": f"Could not retrieve user {id}"
            }), 500
        
    return usersApp
