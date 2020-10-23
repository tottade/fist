{
    name: 'biz',
    subject: [{
        label: 'navigation',
        groups: [{
            name: 'biz.navigation.setLeft',
            data: {
                show: false,
                control: true,
                text: '发送'
            },
        }, {
            name: 'biz.navigation.setRight',
            data: {
                show: false,
                control: true,
                text: '发送'
            },
        }, {
            name: 'biz.navigation.setTitle',
            data: {
                title: '我是一个标题'
            },
        }, {
            name: 'biz.navigation.goBack',
            data: {},
        }, {
            name: 'biz.navigation.close',
            data: {},
        }, {
            name: 'biz.navigation.setMenu',
            data: {
                backgroundColor: '#ADD8E6',
                textColor: '#ADD8E611',
                items: [{
                    id: '1',
                    iconId: 'file',
                    text: '帮助',
                }, {
                    id: '2',
                    iconId: 'photo',
                    text: 'dierge',
                }, ],
            },
        }, ],
    }, {
        label: 'map',
        groups: [{
            name: 'biz.map.locate',
            data: {
                latitude: 39.903578,
                longitude: 116.473565,
            },
        }, {
            name: 'biz.map.search',
            data: {
                latitude: 39.903578,
                longitude: 116.473565,
                scope: 500,
            },
        }, {
            name: 'biz.map.view',
            data: {
                latitude: 39.903578,
                longitude: 116.473565,
                title: '北京国家广告产业园',
            },
        }, ],
    }, {
        label: 'util',
        groups: [{
            name: 'biz.util.fetchImageData',
            data: {},
        }, {
            name: 'biz.util.openLink',
            data: {
                url: 'http://www.ikcrm.com'
            },
        }, {
            name: 'biz.util.datePicker',
            data: {
                format: 'yyyy-MM-dd'
            },
        }, {
            name: 'biz.util.timePicker',
            data: {
                format: 'HH:mm'
            },
        }, {
            name: 'biz.util.dateTimePicker',
            data: {
                format: 'yyyy-MM-dd HH:mm',
                value: '2018-02-02 17:30'
            },
        }, {
            name: 'biz.util.uploadImage',
            data: {
                compression: true,
                multiple: false,
                max: 3,
                quality: 50,
                resize: 50,
                stickers: { // 水印信息
                    time: '17:39',
                    dateWeather: '2018.02.02 周五·晴转多云 7℃',
                    username: 'Shinn',
                    address: '浦东新区·上海',
                },
            },
        }, {
            name: 'biz.util.previewImage',
            data: {
                urls: [
                    'http://gtms01.alicdn.com/tps/i1/TB12i5PHFXXXXaKXVXXY7J9SpXX-500-699.jpeg',
                    'http://gtms04.alicdn.com/tps/i4/TB1E4yUHFXXXXboXFXXK0qsSpXX-500-750.jpeg',
                    'http://gtms02.alicdn.com/tps/i2/TB1Nn1THFXXXXbKXFXX_SFfVFXX-658-658.jpeg',
                ],
                current: 'http://gtms04.alicdn.com/tps/i4/TB1E4yUHFXXXXboXFXXK0qsSpXX-500-750.jpeg',
            },
        }, {
            name: 'biz.util.uploadImageFromCamera',
            data: {
                compression: true,
                quality: 50,
                resize: 50,
                stickers: { // 水印信息
                    time: '17:39',
                    dateWeather: '2018.02.02 周五·晴转多云 7℃',
                    username: 'Shinn',
                    address: '浦东新区·上海',
                },
            },
        }, {
            name: 'biz.util.share',
            data: {
                type: 0,
                url: 'http://www.ikcrm.com',
                title: '分享标题',
                content: '分享内容。。。',
                image: 'https://i01.lw.aliimg.com/tfs/TB1TUovHXXXXXbCXpXXNC1IYXXXLAIWANGi_1_120_120.jpg',
            },
        }, ],
    }, ],
}, {
    name: 'device',
    subject: [{
        label: 'audio',
        groups: [{
            name: 'device.audio.startRecord',
            data: {},
        }, {
            name: 'device.audio.stopRecord',
            data: {},
        }, {
            name: 'device.audio.onRecordEnd',
            data: {},
        }, {
            name: 'device.audio.download',
            data: {
                mediaId: '@lATOCLhLfc46kUl8zlUmRlM'
            },
        }, {
            name: 'device.audio.play',
            data: {
                localAudioId: 'localAudioId'
            },
        }, {
            name: 'device.audio.pause',
            data: {
                localAudioId: 'localAudioId'
            },
        }, {
            name: 'device.audio.resume',
            data: {
                localAudioId: 'localAudioId'
            },
        }, {
            name: 'device.audio.stop',
            data: {
                localAudioId: 'localAudioId'
            },
        }, {
            name: 'device.audio.onPlayEnd',
            data: {},
        }, {
            name: 'device.audio.translateVoice',
            data: {
                mediaId: '@lATOCLhLfc46kUl8zlUmRlM',
                duration: 5.0
            },
        }, ],
    }, {
        label: 'connection',
        groups: [{
            name: 'device.connection.getNetworkType',
            data: {},
        }, ],
    }, {
        label: 'geolocation',
        groups: [{
            name: 'device.geolocation.get',//定位
            data: {
                targetAccuracy: 100.01,
                coordinate: 23.0041,
                withReGeocode: false,
                useCache: true,
            },
        }],
    }, ],
}, {
    name: 'ui',
    subject: [{
        label: 'webViewBounce',
        groups: [{
            name: 'ui.webViewBounce.enable',
            data: {}
        }, {
            name: 'ui.webViewBounce.disable',
            data: {}
        }, ],
    }, ],
}, {
    name: 'util',
    subject: [{
        label: 'domainStorage',
        groups: [{
            name: 'ui.domainStorage.setItem',
            data: {
                name: 'key',
                value: 'value'
            }
        }, {
            name: 'ui.domainStorage.getItem',
            data: {
                name: 'key'
            }
        }, {
            name: 'ui.domainStorage.removeItem',
            data: {
                name: 'key'
            }
        }, ],
    }, ],
}