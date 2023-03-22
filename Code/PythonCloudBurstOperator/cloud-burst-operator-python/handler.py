import json
import re

def hello(event, context):
    event_body = json.loads(event['body'])
    values = re.split(r"\W", event_body["body"])
    tokens = []
    for v in values:
        tokens.extend(re.split(r"\s+", v))
    response = {"statusCode": 200, "body": json.dumps(tokens)}

    return response
