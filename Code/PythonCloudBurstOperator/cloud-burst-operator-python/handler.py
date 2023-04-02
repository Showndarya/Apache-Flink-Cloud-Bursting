import json
import re

def hello(event, context):
    print(event)
    responses=[]
    for text in event["body"]:
        values = re.split(r"\W", text)
        tokens = []
        for v in values:
            tokens.extend(re.split(r"\s+", v))
        response = json.dumps(tokens)
        responses.append(response)

    return responses
