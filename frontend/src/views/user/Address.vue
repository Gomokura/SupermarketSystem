<template>
  <div class="page-container">
    <div class="page-header">
      <h2>收货地址</h2>
      <el-button type="primary" @click="openDialog(false)">添加地址</el-button>
    </div>

    <el-table :data="addresses" border style="width: 100%; margin-top: 20px" v-loading="loading">
      <el-table-column prop="receiver" label="收货人" width="120" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column label="所在地区" min-width="200">
        <template #default="{ row }">
          {{ row.province || '' }}{{ row.city || '' }}{{ row.district || '' }}
        </template>
      </el-table-column>
      <el-table-column prop="detail" label="详细地址" min-width="250" />
      <el-table-column label="默认" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isDefault === 1" type="success">默认</el-tag>
          <el-link v-else type="primary" :underline="false" @click="setDefault(row)">设为默认</el-link>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(true, row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteAddress(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地址' : '添加地址'" width="560px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入收货人" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="form.province" placeholder="省" @change="onProvinceChange" style="width: 100%">
                <el-option v-for="p in provinceList" :key="p" :label="p" :value="p" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.city" placeholder="市" @change="onCityChange" :disabled="!form.province" style="width: 100%">
                <el-option v-for="c in cityList" :key="c" :label="c" :value="c" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.district" placeholder="区/县" :disabled="!form.city" style="width: 100%">
                <el-option v-for="d in districtList" :key="d" :label="d" :value="d" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="请输入详细地址（街道、门牌号等）" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :true-value="1" :false-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addressAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const addresses = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

const formRef = ref()

const form = reactive({
  addressId: null,
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

const rules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

// 省市区静态数据（全国主要省市区，完整版）
const regionData = {
  '北京市': { '北京市': ['东城区', '西城区', '朝阳区', '丰台区', '石景山区', '海淀区', '门头沟区', '房山区', '通州区', '顺义区', '昌平区', '大兴区', '怀柔区', '平谷区', '密云区', '延庆区'] },
  '天津市': { '天津市': ['和平区', '河东区', '河西区', '南开区', '河北区', '红桥区', '东丽区', '西青区', '津南区', '北辰区', '武清区', '宝坻区', '滨海新区', '宁河区', '静海区', '蓟州区'] },
  '上海市': { '上海市': ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '闵行区', '宝山区', '嘉定区', '浦东新区', '金山区', '松江区', '青浦区', '奉贤区', '崇明区'] },
  '重庆市': { '重庆市': ['万州区', '渝中区', '江北区', '沙坪坝区', '九龙坡区', '南岸区', '北碚区', '渝北区', '巴南区', '长寿区', '璧山区', '大足区', '荣昌区', '铜梁区', '涪陵区', '武隆区', '其他'] },
  '广东省': {
    '广州市': ['越秀区', '海珠区', '荔湾区', '天河区', '白云区', '黄埔区', '番禺区', '花都区', '南沙区', '从化区', '增城区'],
    '深圳市': ['罗湖区', '福田区', '南山区', '宝安区', '龙岗区', '盐田区', '龙华区', '坪山区', '光明区'],
    '东莞市': ['莞城街道', '南城街道', '东城街道', '万江街道', '石碣镇', '石龙镇', '茶山镇', '石排镇', '企石镇', '横沥镇', '桥头镇', '谢岗镇', '东坑镇', '常平镇', '寮步镇', '大朗镇', '麻涌镇', '中堂镇', '高埗镇', '樟木头镇', '大岭山镇', '望牛墩镇', '黄江镇', '洪梅镇', '清溪镇', '沙田镇', '道滘镇', '塘厦镇', '虎门镇', '厚街镇', '凤岗镇', '长安镇'],
    '佛山市': ['禅城区', '南海区', '顺德区', '三水区', '高明区'],
    '珠海市': ['香洲区', '斗门区', '金湾区'],
    '中山市': ['石岐街道', '东区街道', '西区街道', '南区街道', '中山港街道', '五桂山街道', '小榄镇', '古镇镇', '横栏镇', '港口镇', '沙溪镇', '大涌镇', '黄圃镇', '南头镇', '东凤镇', '阜沙镇', '三角镇', '民众镇', '南朗镇', '三乡镇', '坦洲镇', '神湾镇'],
    '其他': ['其他']
  },
  '浙江省': {
    '杭州市': ['上城区', '下城区', '江干区', '拱墅区', '西湖区', '滨江区', '萧山区', '余杭区', '临平区', '钱塘区', '富阳区', '临安区', '桐庐县', '淳安县'],
    '宁波市': ['海曙区', '江北区', '北仑区', '镇海区', '鄞州区', '奉化区', '象山县', '宁海县', '余姚市', '慈溪市'],
    '温州市': ['鹿城区', '龙湾区', '瓯海区', '洞头区', '永嘉县', '平阳县', '苍南县', '文成县', '泰顺县', '瑞安市', '乐清市'],
    '其他': ['其他']
  },
  '江苏省': {
    '南京市': ['玄武区', '秦淮区', '建邺区', '鼓楼区', '浦口区', '栖霞区', '雨花台区', '江宁区', '六合区', '溧水区', '高淳区'],
    '苏州市': ['姑苏区', '虎丘区', '吴中区', '相城区', '工业园区', '吴江区', '常熟市', '张家港市', '昆山市', '太仓市'],
    '无锡市': ['锡山区', '惠山区', '滨湖区', '梁溪区', '新吴区', '江阴市', '宜兴市'],
    '其他': ['其他']
  },
  '四川省': {
    '成都市': ['锦江区', '青羊区', '金牛区', '武侯区', '成华区', '龙泉驿区', '青白江区', '新都区', '温江区', '双流区', '郫都区', '新津区', '都江堰市', '彭州市', '邛崃市', '崇州市', '大邑县', '蒲江县'],
    '绵阳市': ['涪城区', '游仙区', '安州区', '三台县', '盐亭县', '梓潼县', '北川羌族自治县', '平武县', '江油市'],
    '其他': ['其他']
  },
  '湖北省': {
    '武汉市': ['江岸区', '江汉区', '硚口区', '汉阳区', '武昌区', '青山区', '洪山区', '东西湖区', '汉南区', '蔡甸区', '江夏区', '黄陂区', '新洲区'],
    '其他': ['其他']
  },
  '湖南省': {
    '长沙市': ['芙蓉区', '天心区', '岳麓区', '开福区', '雨花区', '望城区', '长沙县', '浏阳市', '宁乡市'],
    '其他': ['其他']
  },
  '河南省': {
    '郑州市': ['中原区', '二七区', '管城回族区', '金水区', '惠济区', '上街区', '中牟县', '巩义市', '荥阳市', '新密市', '新郑市', '登封市'],
    '其他': ['其他']
  },
  '山东省': {
    '济南市': ['历下区', '市中区', '槐荫区', '天桥区', '历城区', '长清区', '章丘区', '济阳区', '莱芜区', '钢城区', '平阴县', '商河县'],
    '青岛市': ['市南区', '市北区', '黄岛区', '崂山区', '李沧区', '城阳区', '即墨区', '胶州市', '平度市', '莱西市'],
    '其他': ['其他']
  },
  '福建省': {
    '福州市': ['鼓楼区', '台江区', '仓山区', '马尾区', '晋安区', '长乐区', '闽侯县', '连江县', '罗源县', '闽清县', '永泰县', '平潭县', '福清市'],
    '厦门市': ['思明区', '海沧区', '湖里区', '集美区', '同安区', '翔安区'],
    '泉州市': ['鲤城区', '丰泽区', '洛江区', '泉港区', '惠安县', '安溪县', '永春县', '德化县', '金门县', '石狮市', '晋江市', '南安市'],
    '其他': ['其他']
  },
  '辽宁省': {
    '沈阳市': ['和平区', '沈河区', '大东区', '皇姑区', '铁西区', '苏家屯区', '浑南区', '沈北新区', '于洪区', '辽中区', '康平县', '法库县', '新民市'],
    '大连市': ['中山区', '西岗区', '沙河口区', '甘井子区', '旅顺口区', '金州区', '普兰店区', '长海县', '瓦房店市', '庄河市'],
    '其他': ['其他']
  },
  '陕西省': {
    '西安市': ['新城区', '碑林区', '莲湖区', '灞桥区', '未央区', '雁塔区', '阎良区', '临潼区', '长安区', '高陵区', '鄠邑区', '蓝田县', '周至县'],
    '其他': ['其他']
  },
  '河北省': {
    '石家庄市': ['长安区', '桥西区', '新华区', '井陉矿区', '裕华区', '藁城区', '鹿泉区', '栾城区', '井陉县', '正定县', '行唐县', '灵寿县', '高邑县', '深泽县', '赞皇县', '无极县', '平山县', '元氏县', '赵县', '晋州市', '新乐市'],
    '其他': ['其他']
  },
  '安徽省': {
    '合肥市': ['瑶海区', '庐阳区', '蜀山区', '包河区', '长丰县', '肥东县', '肥西县', '庐江县', '巢湖市'],
    '其他': ['其他']
  },
  '江西省': {
    '南昌市': ['东湖区', '西湖区', '青云谱区', '青山湖区', '新建区', '红谷滩区', '南昌县', '安义县', '进贤县'],
    '其他': ['其他']
  },
  '云南省': {
    '昆明市': ['五华区', '盘龙区', '官渡区', '西山区', '东川区', '呈贡区', '晋宁区', '富民县', '宜良县', '石林彝族自治县', '嵩明县', '禄劝彝族苗族自治县', '寻甸回族彝族自治县', '安宁市'],
    '其他': ['其他']
  },
  '贵州省': {
    '贵阳市': ['南明区', '云岩区', '花溪区', '乌当区', '白云区', '观山湖区', '开阳县', '息烽县', '修文县', '清镇市'],
    '其他': ['其他']
  },
  '广西壮族自治区': {
    '南宁市': ['兴宁区', '青秀区', '江南区', '西乡塘区', '良庆区', '邕宁区', '武鸣区', '隆安县', '马山县', '上林县', '宾阳县', '横州市'],
    '桂林市': ['秀峰区', '叠彩区', '象山区', '七星区', '雁山区', '临桂区', '阳朔县', '灵川县', '全州县', '兴安县', '永福县', '灌阳县', '龙胜各族自治县', '资源县', '平乐县', '荔浦市', '恭城瑶族自治县'],
    '其他': ['其他']
  },
  '海南省': {
    '海口市': ['秀英区', '龙华区', '琼山区', '美兰区'],
    '三亚市': ['海棠区', '吉阳区', '天涯区', '崖州区'],
    '其他': ['其他']
  },
  '内蒙古自治区': {
    '呼和浩特市': ['新城区', '回民区', '玉泉区', '赛罕区', '土默特左旗', '托克托县', '和林格尔县', '清水河县', '武川县'],
    '包头市': ['东河区', '昆都仑区', '青山区', '石拐区', '白云鄂博矿区', '九原区', '土默特右旗', '固阳县', '达尔罕茂明安联合旗'],
    '其他': ['其他']
  },
  '山西省': {
    '太原市': ['小店区', '迎泽区', '杏花岭区', '尖草坪区', '万柏林区', '晋源区', '清徐县', '阳曲县', '娄烦县', '古交市'],
    '其他': ['其他']
  },
  '吉林省': {
    '长春市': ['南关区', '宽城区', '朝阳区', '二道区', '绿园区', '双阳区', '九台区', '农安县', '德惠市', '榆树市'],
    '其他': ['其他']
  },
  '黑龙江省': {
    '哈尔滨市': ['道里区', '南岗区', '道外区', '平房区', '松北区', '香坊区', '呼兰区', '阿城区', '双城区', '依兰县', '方正县', '宾县', '巴彦县', '木兰县', '通河县', '延寿县', '尚志市', '五常市'],
    '其他': ['其他']
  },
  '甘肃省': {
    '兰州市': ['城关区', '七里河区', '西固区', '安宁区', '红古区', '永登县', '皋兰县', '榆中县'],
    '其他': ['其他']
  },
  '新疆维吾尔自治区': {
    '乌鲁木齐市': ['天山区', '沙依巴克区', '新市区', '水磨沟区', '头屯河区', '达坂城区', '米东区', '乌鲁木齐县'],
    '其他': ['其他']
  },
  '宁夏回族自治区': {
    '银川市': ['兴庆区', '西夏区', '金凤区', '永宁县', '贺兰县', '灵武市'],
    '其他': ['其他']
  },
  '青海省': {
    '西宁市': ['城东区', '城中区', '城西区', '城北区', '湟中区', '大通回族土族自治县', '湟源县'],
    '其他': ['其他']
  },
  '西藏自治区': {
    '拉萨市': ['城关区', '堆龙德庆区', '达孜区', '林周县', '当雄县', '尼木县', '曲水县', '墨竹工卡县'],
    '其他': ['其他']
  },
  '广东省_other': {
    '广州市': ['越秀区', '海珠区', '荔湾区', '天河区', '白云区', '黄埔区', '番禺区', '花都区', '南沙区', '从化区', '增城区'],
    '深圳市': ['罗湖区', '福田区', '南山区', '宝安区', '龙岗区', '盐田区', '龙华区', '坪山区', '光明区'],
    '珠海市': ['香洲区', '斗门区', '金湾区'],
    '汕头市': ['龙湖区', '金平区', '濠江区', '潮阳区', '潮南区', '澄海区', '南澳县'],
    '佛山市': ['禅城区', '南海区', '顺德区', '三水区', '高明区'],
    '韶关市': ['武江区', '浈江区', '曲江区', '乐昌市', '南雄市', '始兴县', '仁化县', '翁源县', '新丰县', '乳源瑶族自治县'],
    '湛江市': ['赤坎区', '霞山区', '坡头区', '麻章区', '廉江市', '雷州市', '吴川市', '遂溪县', '徐闻县'],
    '肇庆市': ['端州区', '鼎湖区', '高要区', '四会市', '广宁县', '怀集县', '封开县', '德庆县'],
    '江门市': ['蓬江区', '江海区', '新会区', '台山市', '开平市', '鹤山市', '恩平市'],
    '茂名市': ['茂南区', '电白区', '信宜市', '高州市', '化州市'],
    '惠州市': ['惠城区', '惠阳区', '博罗县', '惠东县', '龙门县'],
    '梅州市': ['梅江区', '梅县区', '兴宁市', '大埔县', '丰顺县', '五华县', '平远县', '蕉岭县'],
    '汕尾市': ['城区', '海丰县', '陆河县', '陆丰市'],
    '河源市': ['源城区', '紫金县', '龙川县', '连平县', '和平县', '东源县'],
    '阳江市': ['江城区', '阳东区', '阳春市', '阳西县'],
    '清远市': ['清城区', '清新区', '英德市', '连州市', '佛冈县', '阳山县', '连山壮族瑶族自治县', '连南瑶族自治县'],
    '东莞市': ['莞城街道', '南城街道', '东城街道', '万江街道', '石碣镇', '石龙镇', '茶山镇', '石排镇', '企石镇', '横沥镇', '桥头镇', '谢岗镇', '东坑镇', '常平镇', '寮步镇', '大朗镇', '麻涌镇', '中堂镇', '高埗镇', '樟木头镇', '大岭山镇', '望牛墩镇', '黄江镇', '洪梅镇', '清溪镇', '沙田镇', '道滘镇', '塘厦镇', '虎门镇', '厚街镇', '凤岗镇', '长安镇'],
    '中山市': ['石岐街道', '东区街道', '西区街道', '南区街道', '中山港街道', '五桂山街道', '小榄镇', '古镇镇', '横栏镇', '港口镇', '沙溪镇', '大涌镇', '黄圃镇', '南头镇', '东凤镇', '阜沙镇', '三角镇', '民众镇', '南朗镇', '三乡镇', '坦洲镇', '神湾镇'],
    '潮州市': ['湘桥区', '潮安区', '饶平县'],
    '揭阳市': ['榕城区', '揭东区', '普宁市', '揭西县', '惠来县'],
    '云浮市': ['云城区', '云安区', '罗定市', '新兴县', '郁南县']
  }
}

const provinceList = Object.keys(regionData).filter(p => !p.endsWith('_other')).sort()
const cityList = ref([])
const districtList = ref([])

const onProvinceChange = () => {
  form.city = ''
  form.district = ''
  districtList.value = []
  if (form.province) {
    const key = form.province + '_other'
    const cities = regionData[key] || regionData[form.province] || {}
    cityList.value = Object.keys(cities).sort()
  } else {
    cityList.value = []
  }
}

const onCityChange = () => {
  form.district = ''
  if (form.province && form.city) {
    const key = form.province + '_other'
    const cities = regionData[key] || regionData[form.province] || {}
    districtList.value = cities[form.city] || []
  } else {
    districtList.value = []
  }
}

const resetForm = () => {
  Object.assign(form, { addressId: null, receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })
  cityList.value = []
  districtList.value = []
}

const openDialog = (edit, row = null) => {
  if (edit && row) {
    isEdit.value = true
    Object.assign(form, row)
    if (form.province) {
      onProvinceChange()
      if (form.city) onCityChange()
    }
  } else {
    isEdit.value = false
    resetForm()
  }
  dialogVisible.value = true
}

onMounted(() => {
  loadAddresses()
})

const loadAddresses = async () => {
  loading.value = true
  try {
    const res = await addressAPI.getList()
    addresses.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const setDefault = async (row) => {
  try {
    await addressAPI.update({ ...row, isDefault: 1 })
    ElMessage.success('已设为默认地址')
    loadAddresses()
  } catch (error) {
    console.error(error)
  }
}

const saveAddress = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = { ...form, userId: userStore.userInfo.userId }
    if (isEdit.value) {
      await addressAPI.update(data)
    } else {
      await addressAPI.add(data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadAddresses()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const deleteAddress = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', { type: 'warning' })
    await addressAPI.delete(row.addressId)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 {
  margin: 0;
}
</style>
