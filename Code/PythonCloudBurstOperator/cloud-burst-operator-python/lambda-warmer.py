import boto3

def lambda_warmer(event, context):
    lambda_client = boto3.client('lambda')
    function_name = context.function_name
    function_version = context.function_version

    # Invoke the function to warm it up
    response = lambda_client.invoke(
        FunctionName=function_name,
        InvocationType='RequestResponse',
        Payload=b'{}'
    )

    # Log the response
    print(response)

    return 'Function {} (version {}) has been warmed up.'.format(function_name, function_version)
