# coding: utf-8

import boto3

def send(sender, reciever, sender_name, message, html):

    # SES の設定
    AWS_REGION = "us-east-1"
    client = boto3.client('ses', region_name = AWS_REGION)

    # 件名
    subject = '【速報】' + sender_name + 'さんがお亡くなりになられました'

    # 送信
    response = client.send_email(
        Source = sender,
        Destination={
            'ToAddresses': [
                reciever,
            ]
        },
        Message = {
            'Subject': {
                'Data': subject,
                'Charset': 'UTF-8'
            },
            'Body': {
                'Text': {
                    'Data': message,
                    'Charset': 'UTF-8'
                },
                'Html': {
                    'Data': html,
                    'Charset': 'UTF-8'
                }
            }
        },
        ReplyToAddresses=[
            sender,
        ]
    )
