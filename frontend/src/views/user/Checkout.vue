<template>
  <div class="page-container">
    <h2>确认订单</h2>

    <!-- 收货地址 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span>收货地址</span>
          <el-button type="primary" size="small" @click="showAddressDialog = true">添加地址</el-button>
        </div>
      </template>
      <el-radio-group v-model="selectedAddressId" v-if="addresses.length">
        <el-radio v-for="addr in addresses" :key="addr.addressId" :value="addr.addressId" class="address-item">
          <div class="address-info">
            <span class="addr-name">{{ addr.receiverName }}</span>
            <span class="addr-phone">{{ addr.phone }}</span>
            <span class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small" type="success">默认</el-tag>
          </div>
        </el-radio>
      </el-radio-group>
      <el-empty v-else description="暂无收货地址，请先添加" />
    </el-card>

    <!-- 商品清单 -->
    <el-card class="section-card">
      <template #header><span>商品清单</span></template>
      <el-table :data="cartItems" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column label="规格" width="120">
          <template #default="{ row }">{{ row.skuName || '-' }}</template>
        </el-table-column>
        <el-table-column label="单价" width="100">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="110">
          <template #default="{ row }">
            <span class="subtotal">￥{{ toYuan(toCents(row.price) * row.quantity) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 优惠与配送 -->
    <el-card class="section-card">
      <template #header><span>优惠与配送</span></template>
      <el-form label-width="100px">
        <!-- 优惠券 -->
        <el-form-item label="优惠券">
          <el-select v-model="selectedCouponId" placeholder="选择优惠券" clearable style="width: 300px" @change="calcPrice">
            <el-option :value="''" label="不使用优惠券" />
            <el-option
              v-for="c in availableCoupons"
              :key="c.userCouponId"
              :value="c.userCouponId"
              :label="couponLabel(c)"
            />
          </el-select>
        </el-form-item>

        <!-- 积分抵扣 -->
        <el-form-item label="积分抵扣">
          <el-checkbox v-model="usePoints" @change="calcPrice">
            使用积分（当前 {{ userPoints }} 分，最多抵扣 ￥{{ maxPointsDeduction.toFixed(2) }}）
          </el-checkbox>
        </el-form-item>

        <!-- 期望配送时间 -->
        <el-form-item label="配送时间">
          <el-select v-model="deliveryDate" style="width: 120px; margin-right: 8px">
            <el-option label="今日" value="today" />
            <el-option label="明日" value="tomorrow" />
          </el-select>
          <el-select v-model="deliveryTime" style="width: 120px">
            <el-option label="上午 9-12点" value="morning" />
            <el-option label="下午 14-18点" value="afternoon" />
            <el-option label="晚上 19-21点" value="evening" />
          </el-select>
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input v-model="remark" type="textarea" :rows="2" placeholder="选填，对本次订单的备注" style="width: 400px" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 支付方式 -->
    <el-card class="section-card">
      <template #header><span>支付方式</span></template>
      <el-radio-group v-model="paymentMethod">
        <el-radio value="WECHAT">微信支付</el-radio>
        <el-radio value="ALIPAY">支付宝</el-radio>
        <el-radio value="BANK">银行卡</el-radio>
        <el-radio value="COD">货到付款</el-radio>
      </el-radio-group>
    </el-card>

    <!-- 价格明细 + 提交 -->
    <div class="order-summary">
      <div class="price-detail">
        <div class="price-row">
          <span>商品总价：</span>
          <span>￥{{ productTotal.toFixed(2) }}</span>
        </div>
        <div class="price-row" v-if="couponDiscount > 0">
          <span>优惠券减免：</span>
          <span class="discount">-￥{{ couponDiscount.toFixed(2) }}</span>
        </div>
        <div class="price-row" v-if="pointsDeduction > 0">
          <span>积分抵扣：</span>
          <span class="discount">-￥{{ pointsDeduction.toFixed(2) }}</span>
        </div>
        <div class="price-row total-row">
          <span>实付金额：</span>
          <span class="total-price">￥{{ actualAmount.toFixed(2) }}</span>
        </div>
      </div>
      <el-button type="primary" size="large" @click="submitOrder" :loading="submitting">提交订单</el-button>
    </div>

    <!-- 添加地址弹窗 -->
    <el-dialog v-model="showAddressDialog" title="添加收货地址" width="560px">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="90px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="addressForm.province" placeholder="省" @change="onProvinceChange" style="width: 100%">
                <el-option v-for="p in provinceList" :key="p" :label="p" :value="p" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="addressForm.city" placeholder="市" @change="onCityChange" :disabled="!addressForm.province" style="width: 100%">
                <el-option v-for="c in cityList" :key="c" :label="c" :value="c" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="addressForm.district" placeholder="区/县" :disabled="!addressForm.city" style="width: 100%">
                <el-option v-for="d in districtList" :key="d" :label="d" :value="d" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" placeholder="请输入详细地址（街道、门牌号等）" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="addAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cartAPI, addressAPI, orderAPI, couponAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const cartItems = ref([])
const addresses = ref([])
const availableCoupons = ref([])
const selectedAddressId = ref(null)
const selectedCouponId = ref('')
const usePoints = ref(false)
const paymentMethod = ref('wechat')
const remark = ref('')
const deliveryDate = ref('today')
const deliveryTime = ref('morning')
const showAddressDialog = ref(false)
const submitting = ref(false)
const addressFormRef = ref()

const addressForm = reactive({ receiverName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })
const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区/县', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const regionData = {
  '北京市': { '市辖区': ['东城区', '西城区', '朝阳区', '丰台区', '石景山区', '海淀区', '门头沟区', '房山区', '通州区', '顺义区', '昌平区', '大兴区', '怀柔区', '平谷区', '密云区', '延庆区'] },
  '上海市': { '市辖区': ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '闵行区', '宝山区', '嘉定区', '浦东新区', '金山区', '松江区', '青浦区', '奉贤区', '崇明区'] },
  '广东省': {
    '广州市': ['越秀区', '海珠区', '荔湾区', '天河区', '白云区', '黄埔区', '番禺区', '花都区', '南沙区', '从化区', '增城区'],
    '深圳市': ['罗湖区', '福田区', '南山区', '宝安区', '龙岗区', '盐田区', '龙华区', '坪山区', '光明区'],
    '东莞市': ['莞城区', '南城区', '万江区', '石碣镇', '石龙镇', '茶山镇', '石排镇', '企石镇', '横沥镇', '常平镇', '东坑镇', '桥头镇', '谢岗镇'],
    '佛山市': ['禅城区', '南海区', '顺德区', '三水区', '高明区'],
    '珠海市': ['香洲区', '斗门区', '金湾区'],
    '中山市': ['石岐区', '东区', '西区', '南区', '中山港街道', '小榄镇', '古镇镇', '横栏镇', '港口镇', '沙溪镇'],
    '惠州市': ['惠城区', '惠阳区', '博罗县', '惠东县', '龙门县'],
    '江门市': ['蓬江区', '江海区', '新会区', '台山市', '开平市', '鹤山市', '恩平市'],
    '汕头市': ['龙湖区', '金平区', '濠江区', '潮阳区', '潮南区', '澄海区', '南澳县']
  },
  '浙江省': {
    '杭州市': ['上城区', '下城区', '江干区', '拱墅区', '西湖区', '滨江区', '萧山区', '余杭区', '临平区', '钱塘区', '富阳区', '临安区', '桐庐县', '淳安县', '建德市'],
    '宁波市': ['海曙区', '江北区', '北仑区', '镇海区', '鄞州区', '奉化区', '象山县', '宁海县', '余姚市', '慈溪市'],
    '温州市': ['鹿城区', '龙湾区', '瓯海区', '洞头区', '永嘉县', '平阳县', '苍南县', '文成县', '泰顺县', '瑞安市', '乐清市', '龙港市'],
    '嘉兴市': ['南湖区', '秀洲区', '嘉善县', '海盐县', '海宁市', '平湖市', '桐乡市'],
    '湖州市': ['吴兴区', '南浔区', '德清县', '长兴县', '安吉县'],
    '绍兴市': ['越城区', '柯桥区', '上虞区', '新昌县', '诸暨市', '嵊州市'],
    '金华市': ['婺城区', '金东区', '武义县', '浦江县', '磐安县', '兰溪市', '义乌市', '东阳市', '永康市'],
    '台州市': ['椒江区', '黄岩区', '路桥区', '三门县', '天台县', '仙居县', '温岭市', '临海市', '玉环市'],
    '苏州市': ['姑苏区', '虎丘区', '吴中区', '相城区', '吴江区', '苏州工业园区', '常熟市', '张家港市', '昆山市', '太仓市'],
    '南京市': ['玄武区', '秦淮区', '建邺区', '鼓楼区', '浦口区', '栖霞区', '雨花台区', '江宁区', '六合区', '溧水区', '高淳区'],
    '无锡市': ['锡山区', '惠山区', '滨湖区', '梁溪区', '新吴区', '江阴市', '宜兴市']
  },
  '江苏省': {
    '南京市': ['玄武区', '秦淮区', '建邺区', '鼓楼区', '浦口区', '栖霞区', '雨花台区', '江宁区', '六合区', '溧水区', '高淳区'],
    '苏州市': ['姑苏区', '虎丘区', '吴中区', '相城区', '吴江区', '苏州工业园区', '常熟市', '张家港市', '昆山市', '太仓市'],
    '无锡市': ['锡山区', '惠山区', '滨湖区', '梁溪区', '新吴区', '江阴市', '宜兴市'],
    '徐州市': ['云龙区', '鼓楼区', '贾汪区', '泉山区', '铜山区', '新沂市', '邳州市', '丰县', '沛县', '睢宁县'],
    '常州市': ['天宁区', '钟楼区', '新北区', '武进区', '金坛区', '溧阳市'],
    '南通市': ['崇川区', '通州区', '海门区', '启东市', '如皋市', '海安市', '如东县'],
    '连云港市': ['连云区', '海州区', '赣榆区', '东海县', '灌云县', '灌南县'],
    '淮安市': ['清江浦区', '淮安区', '淮阴区', '洪泽区', '涟水县', '盱眙县', '金湖县'],
    '盐城市': ['亭湖区', '盐都区', '大丰区', '东台市', '响水县', '滨海县', '阜宁县', '射阳县', '建湖县'],
    '扬州市': ['广陵区', '邗江区', '江都区', '仪征市', '高邮市', '宝应县'],
    '镇江市': ['京口区', '润州区', '丹徒区', '丹阳市', '扬中市', '句容市'],
    '泰州市': ['海陵区', '高港区', '姜堰区', '兴化市', '靖江市', '泰兴市'],
    '宿迁市': ['宿城区', '宿豫区', '沭阳县', '泗阳县', '泗洪县']
  },
  '山东省': {
    '济南市': ['历下区', '市中区', '槐荫区', '天桥区', '历城区', '长清区', '章丘区', '济阳区', '莱芜区', '钢城区', '平阴县', '商河县'],
    '青岛市': ['市南区', '市北区', '黄岛区', '崂山区', '李沧区', '城阳区', '即墨区', '胶州市', '平度市', '莱西市'],
    '烟台市': ['芝罘区', '福山区', '牟平区', '莱山区', '蓬莱区', '龙口市', '莱阳市', '莱州市', '招远市', '栖霞市', '海阳市'],
    '威海市': ['环翠区', '文登区', '荣成市', '乳山市'],
    '潍坊市': ['潍城区', '寒亭区', '坊子区', '奎文区', '青州市', '诸城市', '寿光市', '安丘市', '高密市', '昌邑市', '临朐县', '昌乐县'],
    '淄博市': ['淄川区', '张店区', '博山区', '临淄区', '周村区', '桓台县', '高青县', '沂源县'],
    '临沂市': ['兰山区', '罗庄区', '河东区', '沂南县', '郯城县', '沂水县', '兰陵县', '费县', '平邑县', '莒南县', '蒙阴县', '临沭县'],
    '济宁市': ['任城区', '兖州区', '曲阜市', '邹城市', '微山县', '鱼台县', '金乡县', '嘉祥县', '汶上县', '泗水县', '梁山县']
  },
  '四川省': {
    '成都市': ['锦江区', '青羊区', '金牛区', '武侯区', '成华区', '龙泉驿区', '青白江区', '新都区', '温江区', '双流区', '郫都区', '新津区', '金堂县', '大邑县', '蒲江县', '都江堰市', '彭州市', '邛崃市', '崇州市', '简阳市'],
    '绵阳市': ['涪城区', '游仙区', '安州区', '三台县', '盐亭县', '梓潼县', '北川羌族自治县', '平武县', '江油市'],
    '德阳市': ['旌阳区', '罗江区', '中江县', '广汉市', '什邡市', '绵竹市'],
    '南充市': ['顺庆区', '高坪区', '嘉陵区', '南部县', '营山县', '蓬安县', '仪陇县', '西充县', '阆中市'],
    '宜宾市': ['翠屏区', '南溪区', '叙州区', '江安县', '长宁县', '高县', '珙县', '筠连县', '兴文县', '屏山县']
  },
  '湖北省': {
    '武汉市': ['江岸区', '江汉区', '硚口区', '汉阳区', '武昌区', '青山区', '洪山区', '东西湖区', '汉南区', '蔡甸区', '江夏区', '黄陂区', '新洲区'],
    '宜昌市': ['西陵区', '伍家岗区', '点军区', '猇亭区', '夷陵区', '远安县', '兴山县', '秭归县', '长阳土家族自治县', '五峰土家族自治县', '宜都市', '当阳市', '枝江市'],
    '襄阳市': ['襄城区', '樊城区', '襄州区', '南漳县', '谷城县', '保康县', '老河口市', '枣阳市', '宜城市'],
    '荆州市': ['沙市区', '荆州区', '公安县', '监利县', '江陵县', '石首市', '洪湖市', '松滋市']
  },
  '湖南省': {
    '长沙市': ['芙蓉区', '天心区', '岳麓区', '开福区', '雨花区', '望城区', '长沙县', '浏阳市', '宁乡市'],
    '株洲市': ['荷塘区', '芦淞区', '石峰区', '天元区', '渌口区', '攸县', '茶陵县', '炎陵县', '醴陵市'],
    '湘潭市': ['雨湖区', '岳塘区', '湘潭县', '湘乡市', '韶山市'],
    '衡阳市': ['珠晖区', '雁峰区', '石鼓区', '蒸湘区', '南岳区', '衡阳县', '衡南县', '衡山县', '衡东县', '祁东县', '耒阳市', '常宁市'],
    '岳阳市': ['岳阳楼区', '云溪区', '君山区', '岳阳县', '华容县', '湘阴县', '平江县', '汨罗市', '临湘市'],
    '常德市': ['武陵区', '鼎城区', '安乡县', '汉寿县', '澧县', '临澧县', '桃源县', '石门县', '津市市']
  },
  '河南省': {
    '郑州市': ['中原区', '二七区', '管城回族区', '金水区', '上街区', '惠济区', '中牟县', '巩义市', '荥阳市', '新密市', '新郑市', '登封市'],
    '开封市': ['龙亭区', '顺河回族区', '鼓楼区', '禹王台区', '祥符区', '杞县', '通许县', '尉氏县', '兰考县'],
    '洛阳市': ['老城区', '西工区', '瀍河回族区', '涧西区', '偃师区', '孟津区', '新安县', '栾川县', '嵩县', '汝阳县', '宜阳县', '洛宁县', '伊川县'],
    '南阳市': ['宛城区', '卧龙区', '南召县', '方城县', '西峡县', '镇平县', '内乡县', '淅川县', '社旗县', '唐河县', '新野县', '桐柏县', '邓州市'],
    '新乡市': ['红旗区', '卫滨区', '凤泉区', '牧野区', '新乡县', '获嘉县', '原阳县', '延津县', '封丘县', '卫辉市', '辉县市', '长垣市'],
    '商丘市': ['梁园区', '睢阳区', '民权县', '睢县', '宁陵县', '柘城县', '虞城县', '夏邑县', '永城市']
  },
  '河北省': {
    '石家庄市': ['长安区', '桥西区', '新华区', '井陉矿区', '裕华区', '藁城区', '鹿泉区', '栾城区', '井陉县', '正定县', '行唐县', '灵寿县', '高邑县', '深泽县', '赞皇县', '无极县', '平山县', '元氏县', '赵县', '晋州市', '新乐市', '辛集市'],
    '唐山市': ['路南区', '路北区', '古冶区', '开平区', '丰南区', '曹妃甸区', '滦南县', '乐亭县', '迁西县', '玉田县', '唐海县', '遵化市', '迁安市', '滦州市'],
    '保定市': ['竞秀区', '莲池区', '满城区', '清苑区', '徐水区', '涞水县', '阜平县', '定兴县', '唐县', '高阳县', '容城县', '涞源县', '望都县', '安新县', '易县', '曲阳县', '蠡县', '顺平县', '博野县', '雄县', '涿州市', '定州市', '安国市', '高碑店市'],
    '廊坊市': ['安次区', '广阳区', '固安县', '永清县', '香河县', '大城县', '文安县', '大厂回族自治县', '霸州市', '三河市']
  },
  '福建省': {
    '福州市': ['鼓楼区', '台江区', '仓山区', '马尾区', '晋安区', '长乐区', '闽侯县', '连江县', '罗源县', '闽清县', '永泰县', '平潭县', '福清市'],
    '厦门市': ['思明区', '海沧区', '湖里区', '集美区', '同安区', '翔安区'],
    '泉州市': ['鲤城区', '丰泽区', '洛江区', '泉港区', '惠安县', '安溪县', '永春县', '德化县', '金门县', '石狮市', '晋江市', '南安市'],
    '漳州市': ['芗城区', '龙文区', '龙海区', '长泰区', '云霄县', '漳浦县', '诏安县', '东山县', '南靖县', '平和县', '华安县'],
    '莆田市': ['城厢区', '涵江区', '荔城区', '秀屿区', '仙游县']
  },
  '安徽省': {
    '合肥市': ['瑶海区', '庐阳区', '蜀山区', '包河区', '长丰县', '肥东县', '肥西县', '庐江县', '巢湖市'],
    '芜湖市': ['镜湖区', '弋江区', '鸠江区', '三山区', '芜湖县', '繁昌县', '南陵县', '无为市'],
    '蚌埠市': ['龙子湖区', '蚌山区', '禹会区', '淮上区', '怀远县', '五河县', '固镇县'],
    '淮南市': ['大通区', '田家庵区', '谢家集区', '八公山区', '潘集区', '凤台县', '寿县'],
    '马鞍山市': ['花山区', '雨山区', '博望区', '当涂县', '含山县', '和县'],
    '安庆市': ['迎江区', '大观区', '宜秀区', '怀宁县', '太湖县', '宿松县', '望江县', '岳西县', '桐城市', '潜山市']
  },
  '辽宁省': {
    '沈阳市': ['和平区', '沈河区', '大东区', '皇姑区', '铁西区', '苏家屯区', '浑南区', '沈北新区', '于洪区', '辽中区', '康平县', '法库县', '新民市'],
    '大连市': ['中山区', '西岗区', '沙河口区', '甘井子区', '旅顺口区', '金州区', '普兰店区', '长海县', '瓦房店市', '庄河市'],
    '鞍山市': ['铁东区', '铁西区', '立山区', '千山区', '台安县', '岫岩满族自治县', '海城市']
  },
  '吉林省': {
    '长春市': ['南关区', '宽城区', '朝阳区', '二道区', '绿园区', '双阳区', '九台区', '农安县', '德惠市', '榆树市'],
    '吉林市': ['昌邑区', '龙潭区', '船营区', '丰满区', '永吉县', '蛟河市', '桦甸市', '舒兰市', '磐石市']
  },
  '黑龙江省': {
    '哈尔滨市': ['道里区', '南岗区', '道外区', '平房区', '松北区', '香坊区', '呼兰区', '阿城区', '双城区', '依兰县', '方正县', '宾县', '巴彦县', '木兰县', '通河县', '延寿县', '尚志市', '五常市'],
    '齐齐哈尔市': ['龙沙区', '建华区', '铁锋区', '昂昂溪区', '富拉尔基区', '碾子山区', '梅里斯达斡尔族区', '龙江县', '依安县', '泰来县', '甘南县', '富裕县', '克山县', '克东县', '拜泉县', '讷河市'],
    '大庆市': ['萨尔图区', '龙凤区', '让胡路区', '红岗区', '大同区', '肇州县', '肇源县', '林甸县', '杜尔伯特蒙古族自治县']
  },
  '云南省': {
    '昆明市': ['五华区', '盘龙区', '官渡区', '西山区', '东川区', '呈贡区', '晋宁区', '富民县', '宜良县', '石林彝族自治县', '嵩明县', '禄劝彝族苗族自治县', '寻甸回族彝族自治县', '安宁市'],
    '曲靖市': ['麒麟区', '沾益区', '马龙区', '陆良县', '师宗县', '罗平县', '富源县', '会泽县', '宣威市'],
    '玉溪市': ['红塔区', '江川区', '澄江县', '通海县', '华宁县', '易门县', '峨山彝族自治县', '新平彝族傣族自治县', '元江哈尼族彝族傣族自治县']
  },
  '陕西省': {
    '西安市': ['新城区', '碑林区', '莲湖区', '灞桥区', '未央区', '雁塔区', '阎良区', '临潼区', '长安区', '高陵区', '鄠邑区', '蓝田县', '周至县'],
    '宝鸡市': ['渭滨区', '金台区', '陈仓区', '凤翔县', '岐山县', '扶风县', '眉县', '陇县', '千阳县', '麟游县', '凤县', '太白县'],
    '咸阳市': ['秦都区', '杨陵区', '渭城区', '三原县', '泾阳县', '乾县', '礼泉县', '永寿县', '长武县', '旬邑县', '淳化县', '武功县', '兴平市', '彬州市']
  },
  '重庆市': {
    '市辖区': ['万州区', '涪陵区', '渝中区', '大渡口区', '江北区', '沙坪坝区', '九龙坡区', '南岸区', '北碚区', '渝北区', '巴南区', '黔江区', '长寿区', '江津区', '合川区', '永川区', '南川区', '璧山区', '铜梁区', '潼南区', '荣昌区', '开州区', '梁平区', '武隆区']
  },
  '天津市': {
    '市辖区': ['和平区', '河东区', '河西区', '南开区', '河北区', '红桥区', '东丽区', '西青区', '津南区', '北辰区', '武清区', '宝坻区', '滨海新区', '宁河区', '静海区', '蓟州区']
  }
}

const provinceList = Object.keys(regionData).sort()
const cityList = ref([])
const districtList = ref([])

const onProvinceChange = () => {
  addressForm.city = ''
  addressForm.district = ''
  districtList.value = []
  if (addressForm.province) {
    cityList.value = Object.keys(regionData[addressForm.province] || {}).sort()
  } else {
    cityList.value = []
  }
}

const onCityChange = () => {
  addressForm.district = ''
  if (addressForm.province && addressForm.city) {
    districtList.value = regionData[addressForm.province]?.[addressForm.city] || []
  } else {
    districtList.value = []
  }
}

const toCents = (price) => Math.round(Number(price) * 100)
const toYuan = (cents) => (Number(cents) / 100).toFixed(2)

const userPoints = computed(() => userStore.userInfo?.points || 0)
const productTotal = computed(() => {
  const totalCents = cartItems.value.reduce((s, i) => s + toCents(i.price) * i.quantity, 0)
  return totalCents / 100
})
const maxPointsDeduction = computed(() => {
  const maxByPoints = userPoints.value
  const maxByTotal = Math.round(productTotal.value * 0.3 * 100)
  return Math.min(maxByPoints, maxByTotal) / 100
})
const pointsDeduction = computed(() => usePoints.value ? maxPointsDeduction.value : 0)

const couponDiscount = computed(() => {
  if (!selectedCouponId.value) return 0
  const c = availableCoupons.value.find(x => x.userCouponId === selectedCouponId.value)
  if (!c) return 0
  if (c.couponType === 'full_reduction') return c.discountValue
  if (c.couponType === 'discount') {
    const discountCents = Math.round(productTotal.value * (1 - c.discountValue / 10) * 100)
    return discountCents / 100
  }
  return 0
})

const actualAmount = computed(() => {
  const totalCents = toCents(productTotal.value) - toCents(couponDiscount.value) - toCents(pointsDeduction.value)
  return Math.max(0, totalCents / 100)
})

onMounted(() => {
  loadCart()
  loadAddresses()
})

const loadCart = async () => {
  try {
    const res = await cartAPI.getList()
    cartItems.value = (res.data || []).filter(i => i.selected !== false)
    await loadCoupons()
  } catch (e) { console.error(e) }
}

const loadAddresses = async () => {
  try {
    const res = await addressAPI.getList()
    addresses.value = res.data || []
    const def = addresses.value.find(a => a.isDefault === 1)
    selectedAddressId.value = def?.addressId || addresses.value[0]?.addressId || null
  } catch (e) { console.error(e) }
}

const loadCoupons = async () => {
  if (productTotal.value <= 0) return
  try {
    const res = await couponAPI.getAvailable(productTotal.value)
    availableCoupons.value = res.data || []
    selectBestCoupon()
  } catch (e) { console.error(e) }
}

const selectBestCoupon = () => {
  if (availableCoupons.value.length === 0) {
    selectedCouponId.value = ''
    return
  }
  
  let bestCoupon = null
  let maxDiscount = 0
  
  for (const c of availableCoupons.value) {
    let discount = 0
    if (c.couponType === 'full_reduction') {
      discount = c.discountValue
    } else if (c.couponType === 'discount') {
      discount = productTotal.value * (1 - c.discountValue / 10)
    }
    
    if (discount > maxDiscount) {
      maxDiscount = discount
      bestCoupon = c
    }
  }
  
  if (bestCoupon && maxDiscount > 0) {
    selectedCouponId.value = bestCoupon.userCouponId
  }
}

const calcPrice = async () => {
  if (productTotal.value <= 0) return
  try {
    const res = await couponAPI.getAvailable(productTotal.value)
    availableCoupons.value = res.data || []
    selectBestCoupon()
  } catch (e) { console.error(e) }
}

const couponLabel = (c) => {
  if (c.couponType === 'full_reduction') return `满${c.minOrderAmount}减${c.discountValue}元`
  if (c.couponType === 'discount') return `${c.discountValue}折优惠券`
  return c.couponName
}

const addAddress = async () => {
  await addressFormRef.value.validate()
  try {
    await addressAPI.add(addressForm)
    ElMessage.success('添加成功')
    showAddressDialog.value = false
    loadAddresses()
  } catch (e) { console.error(e) }
}

const submitOrder = async () => {
    if (!selectedAddressId.value) { ElMessage.warning('请选择收货地址'); return }
    if (cartItems.value.length === 0) { ElMessage.warning('购物车为空'); return }

    submitting.value = true
    try {
      const deliveryTimeStr = `${deliveryDate.value === 'today' ? '今日' : '明日'} ${
        { morning: '上午9-12点', afternoon: '下午14-18点', evening: '晚上19-21点' }[deliveryTime.value]
      }`
      const orderData = {
        addressId: selectedAddressId.value,
        paymentMethod: paymentMethod.value,
        couponId: selectedCouponId.value || undefined,
        remark: remark.value || undefined,
        deliveryTimeSlot: deliveryTimeStr,
        items: cartItems.value.map(i => ({ productId: i.productId, skuId: i.skuId, quantity: i.quantity }))
      }
      if (usePoints.value) {
        orderData.pointsUsed = Math.floor(maxPointsDeduction.value * 100)
      }
      await orderAPI.create(orderData)
      
      await cartAPI.clear()
      
      ElMessage.success('订单提交成功')
      router.push('/orders')
    } catch (e) {
      console.error(e)
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
.section-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.address-item { display: block; margin-bottom: 12px; }
.address-info { display: flex; gap: 12px; align-items: center; }
.addr-name { font-weight: 500; }
.addr-phone { color: #666; }
.addr-detail { color: #333; }
.subtotal { color: #f56c6c; font-weight: bold; }
.order-summary { display: flex; justify-content: flex-end; align-items: flex-end; gap: 30px; padding: 20px; background: #fafafa; border-radius: 8px; }
.price-detail { text-align: right; }
.price-row { display: flex; justify-content: space-between; gap: 40px; margin-bottom: 6px; font-size: 14px; }
.discount { color: #67c23a; }
.total-row { font-size: 16px; font-weight: bold; border-top: 1px solid #eee; padding-top: 8px; margin-top: 4px; }
.total-price { color: #f56c6c; font-size: 22px; }
</style>
