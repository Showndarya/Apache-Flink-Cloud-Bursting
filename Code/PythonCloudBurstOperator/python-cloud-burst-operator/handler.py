import json


def hello(event, context):
    event_body = json.loads(event['body'])
    tokens = event_body["body"].split(" ")
    response = {"statusCode": 200, "body": json.dumps(tokens)}

    return response
