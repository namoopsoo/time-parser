
'''
Take a raw file with dumped times, make it json

And push to Amazon.

'''
import os
import datetime
import sys
import re
import requests
from aws_requests_auth.aws_auth import AWSRequestsAuth



def f(mylist, dirname, fnames):
    mylist += fnames
    # mylist.append(fnames)

def get_files_to_parse(raw_dir):

    if not os.path.isdir(raw_dir):
        raise Exception('not a dir')

    files_list = []
    os.path.walk(raw_dir, f, files_list)
    assert files_list

    paths = [os.path.join(raw_dir, filename)
            for filename in files_list
            if re.match(r'.{1,}\.times$', filename)
            ]

    return [path for path in paths
            if not os.path.isdir(path)
            and os.path.exists(path)
            ]

    
def parse_raw_file(input_file):

    with open(input_file) as fd:
        content = fd.read()
        lines = content.split('\n')

        all_dict = {}
        time_list = []

        days = []
        print 'scanning {} lines'.format(len(lines))
        for i, line in enumerate(lines):
            match_day = re.match(r'times (\d{4}-\d{2}-\d{2})', line, re.IGNORECASE)
            match_time = re.match(r'^\d', line)
            blank_line = re.match(r'^$', line)

            if match_day:
                if time_list:
                    all_dict[day] = time_list

                day = match_day.group(1)
                days.append(day)


                time_list = []
            elif match_time:
                # add line
                time_list.append(line)
            elif blank_line:
                pass
            else:
                raise Exception, 'line {} does not conform, "{}"'.format(i, line)
        # End...
        if time_list:
            all_dict[day] = time_list

        payload = from_dict_make_payload(all_dict)

        return payload

def from_dict_make_payload(all_dict):
    out_list = []

    for date, date_list in all_dict.items():
        for time_event in date_list:

            time_event_annotated = date + ';' + time_event

            out_list.append(time_event_annotated)
    payload = {'data': out_list}

    return payload


def push_times_to_aws(data):
    if data and data.get('data'):
        import ipdb; ipdb.set_trace()

        auth = make_auth()
        url = make_time_push_url()
        print auth, url
        response = requests.post(url, auth=auth, json=data)

        print response.status_code
        print response.json()

def make_time_push_url():
    return 'https://{}/staging/time'.format(os.environ.get('API_GATEWAY'))

def make_auth():
    auth = AWSRequestsAuth(aws_access_key=os.environ.get('AWS_ACCESS_KEY'),
                           aws_secret_access_key=os.environ.get('AWS_SECRET_ACCESS_KEY'),
                           aws_host=os.environ.get('API_GATEWAY'),
                           aws_region='us-east-1',
                           aws_service='execute-api')
    return auth

    
def assert_environ():

    assert os.environ.get('API_GATEWAY')
    assert os.environ.get('AWS_ACCESS_KEY')
    assert os.environ.get('AWS_SECRET_ACCESS_KEY')


def get_date(date_str):
    return datetime.datetime.strptime(date_str, '%Y-%m-%d')


def make_summarize_url():
    return 'https://{}/staging/summary/calculate'.format(
            os.environ.get('API_GATEWAY'))


def do_daily_summarize_for_range(start, end):
    delta = (end - start).days
    dates = [
            (start + datetime.timedelta(days=i)).strftime('%Y-%m-%d')
            for i in range(0, delta + 1)]

    payloads = [{"start-date": d,
        "end-date": d}
        for d in dates]

    import ipdb; ipdb.set_trace()

    [do_summarize_with_payload(payload)
            for payload in payloads]


def do_summarize_with_payload(payload):
    url = make_summarize_url()
    auth = make_auth()
    print auth, url
    response = requests.post(url, auth=auth, json=payload)

    print response.status_code
    print response.json()


def do_upload_times_for_files_in_dir(raw_dir):
    files = get_files_to_parse(raw_dir)
    json_payloads = [parse_raw_file(raw_file) for raw_file in files]

    # Push them up.
    [push_times_to_aws(payload) for payload in json_payloads]


if __name__ == '__main__':
    print 'hello'
    assert_environ()

    print sys.argv
    if len(sys.argv) == 4:
        if sys.argv[1] == 'summarize':
            start, end = sys.argv[2], sys.argv[3]
            start, end = get_date(start), get_date(end)
            do_daily_summarize_for_range(start, end)
    elif len(sys.argv) == 3:
        if sys.argv[1] == 'times':
            raw_dir = sys.argv[2]
            do_upload_times_for_files_in_dir(raw_dir)

