from config.db import db
from firebase_admin import firestore
import logging
import uuid


def parse_bool(value, default=False):
    if value is None:
        return default
    if isinstance(value, bool):
        return value
    return str(value).lower() in ["true", "1", "yes", "y", "t"]


def compute_rpe(rir_value, rpe_value):
    if rpe_value is not None:
        return rpe_value
    if rir_value is None:
        return None
    try:
        return max(1.0, 10 - float(rir_value))
    except (TypeError, ValueError):
        return None


def compute_volume(reps_value, weight_value):
    if reps_value is None or weight_value is None:
        return None
    try:
        return float(reps_value) * float(weight_value)
    except (TypeError, ValueError):
        return None


def get_workout_ref(user_id, workout_id):
    workout_ref = db.collection("users").document(user_id).collection("workouts").document(workout_id)
    workout_doc = workout_ref.get()
    if not workout_doc.exists:
        return None, None
    return workout_ref, workout_doc


def process_workout_exercises(user_id, workout_id, exercises_data):
    """
    Iterates through exercises and sets, computes derived fields, and updates PRs.
    Returns the processed exercises list.
    """
    processed_exercises = []

    for exercise_index, exercise in enumerate(exercises_data):
        # Ensure exercise is a dict
        if not isinstance(exercise, dict):
            continue

        processed_exercise = exercise.copy()

        # Get Exercise ID
        exercise_id = processed_exercise.get("exerciseId") or processed_exercise.get("id")

        # Ensure ID exists for the item
        if "id" not in processed_exercise:
            processed_exercise["id"] = str(uuid.uuid4())

        # Process Sets
        sets_data = processed_exercise.get("sets", [])
        processed_sets = []

        # Prepare for PR check - Fetch existing PR once per exercise
        existing_pr_doc = None
        existing_pr_data = {}
        existing_weight = 0
        existing_reps = 0
        pr_fetch_failed = False

        if exercise_id:
            try:
                prs_ref = db.collection("users").document(user_id).collection("prs").document(exercise_id)
                existing_pr_doc = prs_ref.get()
                if existing_pr_doc.exists:
                    existing_pr_data = existing_pr_doc.to_dict()
                    existing_weight = existing_pr_data.get("weight", 0) or 0
                    existing_reps = existing_pr_data.get("reps", 0) or 0
            except Exception as e:
                logging.error(f"Failed to fetch PR for user {user_id}, exercise {exercise_id}: {e}")
                pr_fetch_failed = True

        # Track the best candidate from this workout
        current_best_weight = existing_weight
        current_best_reps = existing_reps
        best_pr_candidate = None
        pr_update_needed = False

        for set_index, set_item in enumerate(sets_data):
            if not isinstance(set_item, dict):
                continue

            processed_set = set_item.copy()

            # Generate IDs if missing (useful for referencing)
            if not processed_set.get("id"):
                processed_set["id"] = f"set_{exercise_index}_{set_index}"

            # Compute RPE/Volume
            rir_value = processed_set.get("rir")
            rpe_value = processed_set.get("rpe")
            reps_value = processed_set.get("reps")
            weight_value = processed_set.get("weight")

            processed_set["rpe"] = compute_rpe(rir_value, rpe_value)
            processed_set["volume"] = compute_volume(reps_value, weight_value)

            # Check PR logic
            incoming_weight = processed_set.get("weight", 0) or 0
            incoming_reps = processed_set.get("reps", 0) or 0

            is_better = False
            explicit_pr = bool(processed_set.get("isPR"))

            if explicit_pr:
                is_better = True
            else:
                if incoming_weight > current_best_weight:
                    is_better = True
                elif incoming_weight == current_best_weight and incoming_reps > current_best_reps:
                    is_better = True

            if is_better:
                current_best_weight = incoming_weight
                current_best_reps = incoming_reps
                best_pr_candidate = {
                    "set": processed_set,
                    "workoutExerciseId": str(exercise_index),
                    "setId": processed_set["id"]
                }
                pr_update_needed = True

            processed_sets.append(processed_set)

        # After processing all sets, write the PR if needed
        if pr_update_needed and best_pr_candidate and exercise_id and not pr_fetch_failed:
            try:
                candidate_set = best_pr_candidate["set"]
                pr_payload = {
                    "exerciseId": exercise_id,
                    "weight": candidate_set.get("weight", 0) or 0,
                    "reps": candidate_set.get("reps", 0) or 0,
                    "rir": candidate_set.get("rir"),
                    "rpe": candidate_set.get("rpe"),
                    "workoutId": workout_id,
                    "workoutExerciseId": best_pr_candidate["workoutExerciseId"],
                    "setId": best_pr_candidate["setId"],
                    "updatedAt": firestore.SERVER_TIMESTAMP,
                }

                # Preserve original createdAt if it existed
                if existing_pr_data and existing_pr_data.get("createdAt"):
                     pr_payload["createdAt"] = existing_pr_data.get("createdAt")
                else:
                     pr_payload["createdAt"] = firestore.SERVER_TIMESTAMP

                # Write to DB
                prs_ref = db.collection("users").document(user_id).collection("prs").document(exercise_id)
                prs_ref.set(pr_payload, merge=True)

            except Exception as e:
                logging.error(f"Failed to update PR for user {user_id}, exercise {exercise_id}: {e}")

        processed_exercise["sets"] = processed_sets
        processed_exercises.append(processed_exercise)

    return processed_exercises
