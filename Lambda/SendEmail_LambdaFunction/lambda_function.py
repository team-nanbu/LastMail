# coding: utf-8

# ライブラリのimport
import datetime
import decimal
import boto3
from boto3.dynamodb.conditions import Key, Attr
from datetime import datetime, timedelta, timezone

import send_email as email

# DynamoDBオブジェクトを取得
dynamodb = boto3.resource('dynamodb')

# Lambdaのメイン関数
def lambda_handler(event, context):

    # 現在時刻を取得
    JST = timezone(timedelta(hours=+9), 'JST')
    time = datetime.now(JST).strftime('%Y年%-m月%-d日 %-H時%-M分')

    # htmlメールテンプレート読み込み
    html_template = open("./mail.html", "r").read()

    # テーブル名を指定
    table_name = "hackathon-mobilehub-342417472-messages"

    # DynamoDBテーブルのオブジェクトを取得
    dynamotable = dynamodb.Table(table_name)

    # 件数取得
    table_json = dynamotable.scan()['Items']
    record_num = len(dynamotable.scan()['Items'])

    # 登録されている友達の数だけ繰り返す
    for i in range(0, record_num):

        # 遺影のURL
        yeah_url = 'https://dtills38xd8p8.cloudfront.net/uploads/yeah_grayscale/' + table_json[i]['userId'] + '.jpg'

        # 遺言メッセージ
        message = table_json[i]['message']

        # 遺言動画のURL
        movie_url = 'https://dtills38xd8p8.cloudfront.net/uploads/movies/' + table_json[i]['movie']

        print(movie_url)

        # html 文字列置換
        html = html_template
        html = html.replace('_senderName', table_json[i]['senderName'])
        html = html.replace('_yeahURL', yeah_url)
        html = html.replace('_lastDate', time)
        html = html.replace('_message', message)
        html = html.replace('_movieURL', movie_url)

        # Email 送信
        email.send(table_json[i]['sender'], table_json[i]['receiver'], table_json[i]['senderName'], message, html)

    result = {
        "isBase64Encoded" : True,
        "statusCode": 200,
        "headers": { 'Content-Type': 'application/json' },
        "body": "JSON string"
    }

    return result
