import json
import re

def hello(event, context):
    data = json.loads(str(event["body"]))
    responses=[]
    for text in data:
        values = re.split(r"\W", text)
        for v in values:
            responses.append(v)
    return responses